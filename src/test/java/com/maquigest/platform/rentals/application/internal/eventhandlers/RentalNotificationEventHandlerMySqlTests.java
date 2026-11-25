package com.maquigest.platform.rentals.application.internal.eventhandlers;

import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestApprovedEvent;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import com.maquigest.platform.rentals.domain.repositories.RentalRequestRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

/** Exercises real repository publication and transaction-bound notification creation. */
@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.notificationPersistenceTests", matches = "true")
class RentalNotificationEventHandlerMySqlTests {
    @Autowired private RentalRequestRepository requests;
    @Autowired private RentalRepository rentals;
    @Autowired private NotificationRepository notifications;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private JdbcTemplate jdbc;

    private TransactionTemplate transaction() {
        var expected = System.getProperty("maquigest.notificationExpectedDb", "");
        assertTrue(expected.matches("maquigest_ddd_notification_[a-f0-9]{8}"));
        assertEquals(expected, jdbc.queryForObject("SELECT DATABASE()", String.class));
        return new TransactionTemplate(transactionManager);
    }

    private RentalRequest pending() {
        return new RentalRequest(null, 801L, 802L, 803L,
                new RentalPeriod(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2)),
                RentalRequestStatus.PENDING, Instant.now());
    }

    private java.util.Optional<com.maquigest.platform.rentals.domain.model.aggregates.Notification> find(
            Long recipient, Long reference, NotificationType type) {
        return notifications.findByRecipientIdAndReferenceIdAndType(new NotificationRecipientId(recipient),
                new NotificationReferenceId(reference), type);
    }

    @Test void creationApprovalDeliveryAndReturnPersistCorrectRecipientsAndAreIdempotent() {
        var tx = transaction();
        Long requestId = tx.execute(status -> requests.save(pending()).getId());
        assertEquals(NotificationEmailStatus.PENDING,
                find(803L, requestId, NotificationType.RENTAL_REQUEST_CREATED).orElseThrow().getEmailStatus());
        assertTrue(find(802L, requestId, NotificationType.RENTAL_REQUEST_CREATED).isEmpty());
        tx.executeWithoutResult(status -> {
            var request = requests.findById(requestId).orElseThrow();
            request.approve();
            requests.save(request);
        });
        var approval = find(802L, requestId, NotificationType.RENTAL_REQUEST_APPROVED).orElseThrow();
        tx.executeWithoutResult(status -> publisher.publishEvent(new RentalRequestApprovedEvent(requestId,
                801L, 802L, 803L, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2))));
        assertEquals(approval.getId(), find(802L, requestId, NotificationType.RENTAL_REQUEST_APPROVED).orElseThrow().getId());
        Long rentalId = tx.execute(status -> rentals.save(new Rental(null, requestId, 801L, 802L, 803L,
                804L, pending().getPeriod(), RentalStatus.CONFIRMED)).getId());
        tx.executeWithoutResult(status -> {
            var rental = rentals.findById(rentalId).orElseThrow();
            rental.deliver();
            rentals.save(rental);
        });
        assertEquals(NotificationEmailStatus.NOT_REQUIRED,
                find(802L, rentalId, NotificationType.RENTAL_DELIVERED).orElseThrow().getEmailStatus());
        tx.executeWithoutResult(status -> {
            var rental = rentals.findById(rentalId).orElseThrow();
            rental.complete();
            rentals.save(rental);
        });
        assertEquals(NotificationEmailStatus.NOT_REQUIRED,
                find(802L, rentalId, NotificationType.RENTAL_RETURNED).orElseThrow().getEmailStatus());
    }

    @Test void rejectionCreatesNotificationAndReloadDoesNotReplayCreation() {
        var tx = transaction();
        Long requestId = tx.execute(status -> requests.save(pending()).getId());
        var created = find(803L, requestId, NotificationType.RENTAL_REQUEST_CREATED).orElseThrow();
        tx.executeWithoutResult(status -> {
            var request = requests.findById(requestId).orElseThrow();
            assertTrue(request.domainEvents().isEmpty());
            request.reject();
            requests.save(request);
        });
        assertEquals(NotificationEmailStatus.PENDING,
                find(802L, requestId, NotificationType.RENTAL_REQUEST_REJECTED).orElseThrow().getEmailStatus());
        assertEquals(created.getId(), find(803L, requestId, NotificationType.RENTAL_REQUEST_CREATED).orElseThrow().getId());
        assertTrue(find(803L, requestId, NotificationType.RENTAL_REQUEST_REJECTED).isEmpty());
    }

    @Test void rollbackDoesNotPersistRequestOrNotification() {
        var tx = transaction();
        var id = new AtomicReference<Long>();
        tx.executeWithoutResult(status -> {
            id.set(requests.save(pending()).getId());
            status.setRollbackOnly();
        });
        assertTrue(requests.findById(id.get()).isEmpty());
        assertTrue(find(803L, id.get(), NotificationType.RENTAL_REQUEST_CREATED).isEmpty());
    }
}

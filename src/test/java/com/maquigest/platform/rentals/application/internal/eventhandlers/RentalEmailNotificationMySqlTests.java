package com.maquigest.platform.rentals.application.internal.eventhandlers;

import com.maquigest.platform.rentals.application.ports.RentalNotificationRecipientPort;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestApprovedEvent;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import com.maquigest.platform.rentals.domain.repositories.RentalRequestRepository;
import com.maquigest.platform.shared.application.email.EmailGateway;
import com.maquigest.platform.shared.application.email.EmailMessage;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.notificationPersistenceTests", matches = "true")
class RentalEmailNotificationMySqlTests {
    @Autowired private RentalRequestRepository requests;
    @Autowired private NotificationRepository notifications;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private EmailGateway gateway;
    @MockitoBean private RentalNotificationRecipientPort recipients;

    @BeforeEach void configure() {
        var expected = System.getProperty("maquigest.notificationExpectedDb", "");
        assertTrue(expected.matches("maquigest_ddd_notification_[a-f0-9]{8}"));
        assertEquals(expected, jdbc.queryForObject("SELECT DATABASE()", String.class));
        when(gateway.isEnabled()).thenReturn(true);
        when(recipients.findActiveEmail(802L)).thenReturn(Optional.of("construction@example.com"));
        when(recipients.findActiveEmail(803L)).thenReturn(Optional.of("rental@example.com"));
    }

    private TransactionTemplate tx() { return new TransactionTemplate(manager); }
    private RentalRequest pending() {
        return new RentalRequest(null, 801L, 802L, 803L,
                new RentalPeriod(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2)),
                RentalRequestStatus.PENDING, Instant.now());
    }
    private NotificationEmailStatus state(Long id, Long recipient, NotificationType type) {
        return notifications.findByRecipientIdAndReferenceIdAndType(new NotificationRecipientId(recipient),
                new NotificationReferenceId(id), type).orElseThrow().getEmailStatus();
    }

    @Test void creationAndApprovalSendOnceAfterCommitAndPersistAcceptedState() {
        Long id = tx().execute(status -> {
            var saved = requests.save(pending());
            verify(gateway, never()).send(any());
            return saved.getId();
        });
        assertEquals(NotificationEmailStatus.SENT, state(id, 803L, NotificationType.RENTAL_REQUEST_CREATED));
        verify(gateway).send(argThat(message -> message.recipient().equals("rental@example.com")));
        tx().executeWithoutResult(status -> {
            var request = requests.findById(id).orElseThrow();
            request.approve(); requests.save(request);
        });
        assertEquals(NotificationEmailStatus.SENT, state(id, 802L, NotificationType.RENTAL_REQUEST_APPROVED));
        tx().executeWithoutResult(status -> publisher.publishEvent(new RentalRequestApprovedEvent(id,
                801L, 802L, 803L, LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 2))));
        verify(gateway, times(2)).send(any(EmailMessage.class));
        verify(gateway).send(argThat(message -> message.recipient().equals("construction@example.com")));
    }

    @Test void rejectionProviderFailureDoesNotRollbackRentalRequest() {
        Long id = tx().execute(status -> requests.save(pending()).getId());
        doThrow(new IllegalStateException("fake transport failure")).when(gateway).send(any());
        tx().executeWithoutResult(status -> {
            var request = requests.findById(id).orElseThrow();
            request.reject(); requests.save(request);
        });
        assertEquals(RentalRequestStatus.REJECTED, requests.findById(id).orElseThrow().getStatus());
        assertEquals(NotificationEmailStatus.FAILED, state(id, 802L, NotificationType.RENTAL_REQUEST_REJECTED));
    }

    @Test void rollbackDoesNotSendAndDisabledTransportKeepsPending() {
        tx().executeWithoutResult(status -> { requests.save(pending()); status.setRollbackOnly(); });
        verify(gateway, never()).send(any());
        when(gateway.isEnabled()).thenReturn(false);
        Long id = tx().execute(status -> requests.save(pending()).getId());
        assertEquals(NotificationEmailStatus.PENDING, state(id, 803L, NotificationType.RENTAL_REQUEST_CREATED));
        verify(gateway, never()).send(any());
    }
}

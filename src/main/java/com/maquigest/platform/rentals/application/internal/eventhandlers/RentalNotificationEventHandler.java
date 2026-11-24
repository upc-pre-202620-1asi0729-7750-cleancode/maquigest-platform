package com.maquigest.platform.rentals.application.internal.eventhandlers;

import com.maquigest.platform.rentals.application.commandservices.NotificationCommandService;
import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.rentals.domain.model.events.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Persists local notifications in the same transaction as the originating fact. */
@Slf4j
@Service
public class RentalNotificationEventHandler {
    private final NotificationCommandService notifications;
    private final RentalRepository rentals;

    public RentalNotificationEventHandler(NotificationCommandService notifications, RentalRepository rentals) {
        this.notifications = notifications;
        this.rentals = rentals;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(RentalRequestCreatedEvent event) {
        create(event.rentalCompanyUserId(), event.requestId(), NotificationType.RENTAL_REQUEST_CREATED, event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(RentalRequestApprovedEvent event) {
        create(event.constructionUserId(), event.requestId(), NotificationType.RENTAL_REQUEST_APPROVED, Instant.now());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(RentalRequestRejectedEvent event) {
        create(event.constructionUserId(), event.requestId(), NotificationType.RENTAL_REQUEST_REJECTED, event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(RentalActivatedEvent event) {
        notifyConstructionCompany(event.rentalId(), NotificationType.RENTAL_DELIVERED);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(RentalCompletedEvent event) {
        notifyConstructionCompany(event.rentalId(), NotificationType.RENTAL_RETURNED);
    }

    private void notifyConstructionCompany(Long rentalId, NotificationType type) {
        var rental = rentals.findById(rentalId);
        if (rental.isEmpty()) {
            // A synthetic or stale signal without a persisted rental has no valid recipient.
            log.warn("Skipping rental notification for missing rental {}", rentalId);
            return;
        }
        create(rental.get().getConstructionUserId(), rentalId, type, Instant.now());
    }

    private void create(Long recipientId, Long referenceId, NotificationType type, Instant occurredAt) {
        var result = notifications.handle(new CreateNotificationCommand(new NotificationRecipientId(recipientId),
                new NotificationReferenceId(referenceId), type, occurredAt));
        if (result.isFailure()) {
            throw new IllegalStateException("Rental notification could not be persisted: " + type);
        }
    }
}

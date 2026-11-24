package com.maquigest.platform.iam.domain.model.aggregates;

import com.maquigest.platform.iam.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationEmailStatus;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;

/** A notification owned by the Iam bounded context. */
@Getter
public final class Notification extends AbstractDomainAggregateRoot<Notification> {
    private final Long id;
    private final NotificationRecipientId recipientId;
    private final NotificationReferenceId referenceId;
    private final NotificationType type;
    private final Instant occurredAt;
    private final java.util.UUID factId;
    private NotificationEmailStatus emailStatus;
    private Instant readAt;

    public Notification(CreateNotificationCommand command) {
        this(null, command.recipientId(), command.referenceId(), command.type(),
                command.occurredAt(), command.factId(), command.type().requiresEmail()
                        ? NotificationEmailStatus.PENDING : NotificationEmailStatus.NOT_REQUIRED, null);
    }

    private Notification(Long id, NotificationRecipientId recipientId,
                         NotificationReferenceId referenceId, NotificationType type,
                         Instant occurredAt, java.util.UUID factId, NotificationEmailStatus emailStatus, Instant readAt) {
        if ((id != null && id < 1) || recipientId == null || referenceId == null ||
                type == null || occurredAt == null || factId == null || emailStatus == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
        if (type.requiresEmail() == (emailStatus == NotificationEmailStatus.NOT_REQUIRED)) {
            throw new IllegalArgumentException("validation.notification.email-status.invalid");
        }
        if (readAt != null && readAt.isBefore(occurredAt)) {
            throw new IllegalArgumentException("validation.notification.read-at.invalid");
        }
        this.id = id;
        this.recipientId = recipientId;
        this.referenceId = referenceId;
        this.type = type;
        this.occurredAt = occurredAt;
        this.factId = factId;
        this.emailStatus = emailStatus;
        this.readAt = readAt;
    }

    public static Notification reconstitute(Long id, NotificationRecipientId recipientId,
            NotificationReferenceId referenceId, NotificationType type, Instant occurredAt, java.util.UUID factId,
            NotificationEmailStatus emailStatus, Instant readAt) {
        if (id == null) throw new IllegalArgumentException("validation.notification.required");
        return new Notification(id, recipientId, referenceId, type, occurredAt, factId, emailStatus, readAt);
    }

    public boolean isRead() { return readAt != null; }

    public void markAsRead(Instant readAt) {
        if (readAt == null || readAt.isBefore(occurredAt)) {
            throw new IllegalArgumentException("validation.notification.read-at.invalid");
        }
        if (!isRead()) this.readAt = readAt;
    }

    public void markEmailAsSent() {
        requirePendingEmail();
        emailStatus = NotificationEmailStatus.SENT;
    }

    public void markEmailAsFailed() {
        requirePendingEmail();
        emailStatus = NotificationEmailStatus.FAILED;
    }

    private void requirePendingEmail() {
        if (emailStatus != NotificationEmailStatus.PENDING) {
            throw new IllegalStateException("validation.notification.email-status.invalid");
        }
    }
}

package com.maquigest.platform.rentals.domain.model.commands;

import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationType;
import java.time.Instant;

/** Requests the creation of a notification for a rental business fact. */
public record CreateNotificationCommand(NotificationRecipientId recipientId,
                                        NotificationReferenceId referenceId,
                                        NotificationType type, Instant occurredAt) {
    public CreateNotificationCommand {
        if (recipientId == null || referenceId == null || type == null || occurredAt == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
    }
}

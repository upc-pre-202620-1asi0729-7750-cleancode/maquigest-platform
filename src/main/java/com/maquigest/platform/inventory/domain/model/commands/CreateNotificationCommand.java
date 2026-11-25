package com.maquigest.platform.inventory.domain.model.commands;

import com.maquigest.platform.inventory.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.inventory.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.inventory.domain.model.valueobjects.NotificationType;
import java.time.Instant;

/** Requests the creation of a notification for a inventory business fact. */
public record CreateNotificationCommand(NotificationRecipientId recipientId,
                                        NotificationReferenceId referenceId,
                                        NotificationType type, Instant occurredAt, java.util.UUID factId) {
    public CreateNotificationCommand {
        if (recipientId == null || referenceId == null || type == null || occurredAt == null || factId == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
    }
}

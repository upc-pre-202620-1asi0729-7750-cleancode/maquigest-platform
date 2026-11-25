package com.maquigest.platform.iam.domain.model.commands;

import com.maquigest.platform.iam.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationType;
import java.time.Instant;

/** Requests the creation of a notification for a iam business fact. */
public record CreateNotificationCommand(NotificationRecipientId recipientId,
                                        NotificationReferenceId referenceId,
                                        NotificationType type, Instant occurredAt, java.util.UUID factId) {
    public CreateNotificationCommand {
        if (recipientId == null || referenceId == null || type == null || occurredAt == null || factId == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
    }
}

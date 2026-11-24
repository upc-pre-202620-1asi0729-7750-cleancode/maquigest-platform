package com.maquigest.platform.iam.domain.model.queries;

import com.maquigest.platform.iam.domain.model.valueobjects.NotificationRecipientId;

/** Finds a single notification within the recipient's own inbox. */
public record GetNotificationByIdQuery(Long id, NotificationRecipientId recipientId) {
    public GetNotificationByIdQuery {
        if (id == null || id < 1 || recipientId == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
    }
}

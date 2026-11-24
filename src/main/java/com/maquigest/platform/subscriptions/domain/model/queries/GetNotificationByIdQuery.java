package com.maquigest.platform.subscriptions.domain.model.queries;

import com.maquigest.platform.subscriptions.domain.model.valueobjects.NotificationRecipientId;

/** Finds a single notification within the recipient's own inbox. */
public record GetNotificationByIdQuery(Long id, NotificationRecipientId recipientId) {
    public GetNotificationByIdQuery {
        if (id == null || id < 1 || recipientId == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
    }
}

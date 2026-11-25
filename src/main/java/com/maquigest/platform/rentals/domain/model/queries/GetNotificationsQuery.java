package com.maquigest.platform.rentals.domain.model.queries;

import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;

/** Lists notifications owned by the authenticated recipient. */
public record GetNotificationsQuery(NotificationRecipientId recipientId) {
    public GetNotificationsQuery {
        if (recipientId == null) throw new IllegalArgumentException("validation.notification-recipient-id.required");
    }
}

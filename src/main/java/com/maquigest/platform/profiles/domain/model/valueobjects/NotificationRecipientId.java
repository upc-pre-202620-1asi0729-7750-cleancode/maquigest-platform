package com.maquigest.platform.profiles.domain.model.valueobjects;

/** Identifies the recipient user within Rentals. */
public record NotificationRecipientId(Long recipientId) {
    public NotificationRecipientId {
        if (recipientId == null || recipientId < 1) {
            throw new IllegalArgumentException("validation.notification-recipient-id.required");
        }
    }
}

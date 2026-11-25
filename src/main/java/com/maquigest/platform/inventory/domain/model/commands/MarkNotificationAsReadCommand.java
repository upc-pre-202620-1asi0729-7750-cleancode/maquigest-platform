package com.maquigest.platform.inventory.domain.model.commands;

import com.maquigest.platform.inventory.domain.model.valueobjects.NotificationRecipientId;

/** Marks one notification as read by its authenticated recipient. */
public record MarkNotificationAsReadCommand(Long id, NotificationRecipientId recipientId) {
    public MarkNotificationAsReadCommand {
        if (id == null || id < 1 || recipientId == null) {
            throw new IllegalArgumentException("validation.notification.required");
        }
    }
}

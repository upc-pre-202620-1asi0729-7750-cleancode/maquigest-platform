package com.maquigest.platform.rentals.domain.model.events;

import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;

/** Local domain event, published once when a new email notification is persisted. */
public record RentalNotificationEmailRequestedEvent(Long notificationId, NotificationRecipientId recipientId) { }

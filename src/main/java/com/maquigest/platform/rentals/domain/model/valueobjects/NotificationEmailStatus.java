package com.maquigest.platform.rentals.domain.model.valueobjects;

/** Email delivery state, independent of the notification's read state. */
public enum NotificationEmailStatus {
    NOT_REQUIRED, PENDING, SENT, FAILED
}

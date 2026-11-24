package com.maquigest.platform.rentals.domain.model.valueobjects;

/** Business facts supported by rental notifications. */
public enum NotificationType {
    RENTAL_REQUEST_CREATED,
    RENTAL_REQUEST_APPROVED,
    RENTAL_REQUEST_REJECTED,
    RENTAL_DELIVERED,
    RENTAL_RETURNED;

    public boolean requiresEmail() {
        return switch (this) {
            case RENTAL_REQUEST_CREATED, RENTAL_REQUEST_APPROVED, RENTAL_REQUEST_REJECTED -> true;
            case RENTAL_DELIVERED, RENTAL_RETURNED -> false;
        };
    }
}

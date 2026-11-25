package com.maquigest.platform.rentals.domain.model.valueobjects;

/** External identity as understood by the rentals bounded context. */
public record RentalCompanyUserId(Long value) {
    public RentalCompanyUserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("rentalCompanyUserId must be positive");
        }
    }
}

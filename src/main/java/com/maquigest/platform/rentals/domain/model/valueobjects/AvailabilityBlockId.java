package com.maquigest.platform.rentals.domain.model.valueobjects;

/** External identity as understood by the rentals bounded context. */
public record AvailabilityBlockId(Long value) {
    public AvailabilityBlockId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("availabilityBlockId must be positive");
        }
    }
}

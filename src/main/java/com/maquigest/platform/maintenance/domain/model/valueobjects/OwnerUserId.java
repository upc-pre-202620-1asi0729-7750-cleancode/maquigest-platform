package com.maquigest.platform.maintenance.domain.model.valueobjects;

/** External identity as understood by the maintenance bounded context. */
public record OwnerUserId(Long value) {
    public OwnerUserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ownerUserId must be positive");
        }
    }
}

package com.maquigest.platform.rentals.domain.model.valueobjects;

/** External identity as understood by the rentals bounded context. */
public record EquipmentId(Long value) {
    public EquipmentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("equipmentId must be positive");
        }
    }
}

package com.maquigest.platform.maintenance.domain.model.valueobjects;

/** External identity as understood by the maintenance bounded context. */
public record EquipmentId(Long value) {
    public EquipmentId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("equipmentId must be positive");
        }
    }
}

package com.maquigest.platform.inventory.domain.model.queries;
public record GetEquipmentByIdQuery(Long equipmentId) {
    public GetEquipmentByIdQuery {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
    }
}

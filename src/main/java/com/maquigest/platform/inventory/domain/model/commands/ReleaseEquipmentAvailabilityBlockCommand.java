package com.maquigest.platform.inventory.domain.model.commands;

/** Release one identified reservation; no client-supplied owner or timestamp. */
public record ReleaseEquipmentAvailabilityBlockCommand(Long equipmentId, Long blockId) {
    public ReleaseEquipmentAvailabilityBlockCommand {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
        if (blockId == null || blockId <= 0) throw new IllegalArgumentException("blockId must be positive");
    }
}

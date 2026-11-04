package com.maquigest.platform.inventory.domain.model.commands;
public record DeleteEquipmentCommand(Long equipmentId) {
    public DeleteEquipmentCommand {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
    }
}

package com.maquigest.platform.inventory.domain.model.commands;

import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;

/** Execute a business-validated operational transition, not an editable-fields PATCH. */
public record ChangeEquipmentOperationalStatusCommand(Long equipmentId, EquipmentStatus status) { }

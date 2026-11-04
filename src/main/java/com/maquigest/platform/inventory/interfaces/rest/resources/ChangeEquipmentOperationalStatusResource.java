package com.maquigest.platform.inventory.interfaces.rest.resources;

import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeEquipmentOperationalStatusResource(@NotNull EquipmentStatus status) { }

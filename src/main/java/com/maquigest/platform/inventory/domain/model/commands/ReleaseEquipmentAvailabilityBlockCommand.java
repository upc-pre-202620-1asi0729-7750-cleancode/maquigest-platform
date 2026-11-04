package com.maquigest.platform.inventory.domain.model.commands;

/** Release one identified reservation; no client-supplied owner or timestamp. */
public record ReleaseEquipmentAvailabilityBlockCommand(Long equipmentId, Long blockId) { }

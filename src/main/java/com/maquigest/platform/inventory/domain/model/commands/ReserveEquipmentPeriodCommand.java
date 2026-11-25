package com.maquigest.platform.inventory.domain.model.commands;

import java.time.LocalDate;

/** Atomically reserve an inclusive date range on one Equipment aggregate. */
public record ReserveEquipmentPeriodCommand(Long equipmentId, LocalDate startDate, LocalDate endDate) {
    public ReserveEquipmentPeriodCommand {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) throw new IllegalArgumentException("Invalid availability date range");
    }
}

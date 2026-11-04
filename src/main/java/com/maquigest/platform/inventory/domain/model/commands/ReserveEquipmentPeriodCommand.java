package com.maquigest.platform.inventory.domain.model.commands;

import java.time.LocalDate;

/** Atomically reserve an inclusive date range on one Equipment aggregate. */
public record ReserveEquipmentPeriodCommand(Long equipmentId, LocalDate startDate, LocalDate endDate) { }

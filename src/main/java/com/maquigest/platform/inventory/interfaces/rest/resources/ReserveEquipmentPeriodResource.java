package com.maquigest.platform.inventory.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Inclusive calendar dates (YYYY-MM-DD), the unit already used by Inventory Domain. */
public record ReserveEquipmentPeriodResource(@NotNull LocalDate startDate, @NotNull LocalDate endDate) { }

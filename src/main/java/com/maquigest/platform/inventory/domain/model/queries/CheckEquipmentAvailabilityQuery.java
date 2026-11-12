package com.maquigest.platform.inventory.domain.model.queries;
import java.time.LocalDate;
public record CheckEquipmentAvailabilityQuery(Long equipmentId, LocalDate startDate, LocalDate endDate) {
    public CheckEquipmentAvailabilityQuery {
        if (equipmentId == null || equipmentId <= 0 || startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Valid equipmentId and ordered start/end dates are required");
        }
    }
}

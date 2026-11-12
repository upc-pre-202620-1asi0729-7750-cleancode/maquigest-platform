package com.maquigest.platform.rentals.domain.model.valueobjects;
import java.time.LocalDate;
public record RentalPeriod(LocalDate startDate, LocalDate endDate) {
    public RentalPeriod {
        if (startDate == null || endDate == null || endDate.isBefore(startDate))
            throw new IllegalArgumentException("Invalid inclusive rental period");
    }
    public boolean overlaps(RentalPeriod other) {
        return !endDate.isBefore(other.startDate()) && !startDate.isAfter(other.endDate());
    }
}

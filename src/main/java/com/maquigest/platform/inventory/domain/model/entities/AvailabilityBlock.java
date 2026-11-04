package com.maquigest.platform.inventory.domain.model.entities;
import java.time.LocalDate;
/** Child entity of Equipment; inclusive calendar dates in the business timezone. */
public final class AvailabilityBlock {
    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    public AvailabilityBlock(Long id, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Availability period must have valid ordered dates");
        }
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean overlaps(LocalDate start, LocalDate end) {
        return !endDate.isBefore(start) && !startDate.isAfter(end);
    }
}

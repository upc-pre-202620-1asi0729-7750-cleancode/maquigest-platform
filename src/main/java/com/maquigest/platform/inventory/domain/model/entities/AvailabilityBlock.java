package com.maquigest.platform.inventory.domain.model.entities;

import java.time.Instant;
import java.time.LocalDate;

/** One dated reservation within an Equipment aggregate; releasing never deletes its history. */
public final class AvailabilityBlock {
    private final Long id;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private Instant releasedAt;

    public AvailabilityBlock(Long id, LocalDate startDate, LocalDate endDate) {
        this(id, startDate, endDate, null);
    }

    public AvailabilityBlock(Long id, LocalDate startDate, LocalDate endDate, Instant releasedAt) {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Availability period must have valid ordered dates");
        }
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.releasedAt = releasedAt;
    }

    public Long getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Instant getReleasedAt() { return releasedAt; }
    public boolean isActive() { return releasedAt == null; }

    /** Idempotent: an already-released reservation remains historical. */
    public void release(Instant when) {
        if (when == null) throw new IllegalArgumentException("Release timestamp is required");
        if (isActive()) releasedAt = when;
    }

    public boolean overlaps(LocalDate start, LocalDate end) {
        return !endDate.isBefore(start) && !startDate.isAfter(end);
    }
}

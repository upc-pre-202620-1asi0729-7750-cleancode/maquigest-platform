package com.maquigest.platform.rentals.domain.model.entities;
public record EquipmentReturn(Long id, Long rentalId, java.time.Instant returnedAt, String notes, boolean maintenanceRequired) {
    public EquipmentReturn {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        if (rentalId == null || rentalId <= 0) throw new IllegalArgumentException("rentalId must be positive");
        if (returnedAt == null) throw new IllegalArgumentException("Timestamp required");
        notes = notes == null ? "" : notes.trim();
        if (notes.length() > 2000) throw new IllegalArgumentException("Notes too long");
    }
}

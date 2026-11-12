package com.maquigest.platform.rentals.domain.model.entities;
public record Delivery(Long id, Long rentalId, java.time.Instant deliveredAt, String notes) {
    public Delivery {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        if (rentalId == null || rentalId <= 0) throw new IllegalArgumentException("rentalId must be positive");
        if (deliveredAt == null) throw new IllegalArgumentException("Timestamp required");
        notes = notes == null ? "" : notes.trim();
        if (notes.length() > 2000) throw new IllegalArgumentException("Notes too long");
    }
}

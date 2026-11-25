package com.maquigest.platform.rentals.domain.model.commands;
public record RegisterReturnCommand(Long rentalId, Long actorUserId, java.time.Instant returnedAt, String notes, boolean maintenanceRequired) {
    public RegisterReturnCommand {
        if (rentalId == null || rentalId <= 0) throw new IllegalArgumentException("rentalId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
        if (returnedAt == null) throw new IllegalArgumentException("returnedAt is required");
    }
}

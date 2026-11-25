package com.maquigest.platform.rentals.domain.model.commands;
public record RegisterDeliveryCommand(Long rentalId, Long actorUserId, java.time.Instant deliveredAt, String notes) {
    public RegisterDeliveryCommand {
        if (rentalId == null || rentalId <= 0) throw new IllegalArgumentException("rentalId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
        if (deliveredAt == null) throw new IllegalArgumentException("deliveredAt is required");
    }
}

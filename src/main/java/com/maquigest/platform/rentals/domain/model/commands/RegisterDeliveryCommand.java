package com.maquigest.platform.rentals.domain.model.commands;
public record RegisterDeliveryCommand(Long rentalId, Long actorUserId, java.time.Instant deliveredAt, String notes) { }

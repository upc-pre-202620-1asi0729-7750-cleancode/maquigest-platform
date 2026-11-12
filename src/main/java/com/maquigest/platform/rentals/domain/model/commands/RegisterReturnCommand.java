package com.maquigest.platform.rentals.domain.model.commands;
public record RegisterReturnCommand(Long rentalId, Long actorUserId, java.time.Instant returnedAt, String notes, boolean maintenanceRequired) { }

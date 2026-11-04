package com.maquigest.platform.rentals.domain.model.commands;
public record ResolveRentalRequestCommand(Long requestId, Long actorUserId, com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus targetStatus) { }

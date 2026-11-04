package com.maquigest.platform.rentals.domain.model.commands;

import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;

public record RescheduleRentalRequestCommand(Long requestId, Long actorUserId, RentalPeriod period) { }

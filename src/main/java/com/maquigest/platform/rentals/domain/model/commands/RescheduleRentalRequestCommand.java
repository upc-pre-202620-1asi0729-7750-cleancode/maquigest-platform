package com.maquigest.platform.rentals.domain.model.commands;

import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;

public record RescheduleRentalRequestCommand(Long requestId, Long actorUserId, RentalPeriod period) {
    public RescheduleRentalRequestCommand {
        if (requestId == null || requestId <= 0) throw new IllegalArgumentException("requestId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
        if (period == null) throw new IllegalArgumentException("period is required");
    }
}

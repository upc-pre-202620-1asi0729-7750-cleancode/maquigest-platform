package com.maquigest.platform.rentals.domain.model.commands;
public record ResolveRentalRequestCommand(Long requestId, Long actorUserId, com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus targetStatus) {
    public ResolveRentalRequestCommand {
        if (requestId == null || requestId <= 0) throw new IllegalArgumentException("requestId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
        if (targetStatus == null) throw new IllegalArgumentException("targetStatus is required");
    }
}

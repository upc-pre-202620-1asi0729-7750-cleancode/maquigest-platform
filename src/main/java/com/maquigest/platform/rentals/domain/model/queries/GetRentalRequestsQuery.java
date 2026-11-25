package com.maquigest.platform.rentals.domain.model.queries;
public record GetRentalRequestsQuery(Long actorUserId) {
    public GetRentalRequestsQuery {
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

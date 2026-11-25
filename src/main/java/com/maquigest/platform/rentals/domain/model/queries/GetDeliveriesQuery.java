package com.maquigest.platform.rentals.domain.model.queries;

public record GetDeliveriesQuery(Long actorUserId) {
    public GetDeliveriesQuery {
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

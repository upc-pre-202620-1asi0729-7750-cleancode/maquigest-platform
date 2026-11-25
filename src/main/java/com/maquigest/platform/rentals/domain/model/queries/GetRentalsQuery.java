package com.maquigest.platform.rentals.domain.model.queries;
public record GetRentalsQuery(Long actorUserId) {
    public GetRentalsQuery {
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

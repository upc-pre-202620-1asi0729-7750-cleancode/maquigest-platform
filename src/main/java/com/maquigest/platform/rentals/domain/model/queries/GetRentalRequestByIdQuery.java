package com.maquigest.platform.rentals.domain.model.queries;
public record GetRentalRequestByIdQuery(Long id, Long actorUserId) {
    public GetRentalRequestByIdQuery {
        if (id == null || id <= 0) throw new IllegalArgumentException("id must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

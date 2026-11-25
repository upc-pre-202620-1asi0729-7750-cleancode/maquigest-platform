package com.maquigest.platform.rentals.domain.model.queries;

public record GetEquipmentReturnsQuery(Long actorUserId) {
    public GetEquipmentReturnsQuery {
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

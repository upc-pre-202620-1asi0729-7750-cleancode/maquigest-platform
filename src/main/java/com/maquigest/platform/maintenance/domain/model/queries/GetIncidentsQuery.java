package com.maquigest.platform.maintenance.domain.model.queries;
public record GetIncidentsQuery(Long actorUserId,Long equipmentId,boolean rentalCompany) {
    public GetIncidentsQuery {
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
        if (equipmentId != null && equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive when provided");
    }
}

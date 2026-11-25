package com.maquigest.platform.maintenance.domain.model.queries;
public record GetMaintenancesQuery(Long actorUserId,Long equipmentId) {
    public GetMaintenancesQuery {
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
        if (equipmentId != null && equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive when provided");
    }
}

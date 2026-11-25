package com.maquigest.platform.maintenance.domain.model.queries;
public record GetMaintenanceByIdQuery(Long maintenanceId,Long actorUserId) {
    public GetMaintenanceByIdQuery {
        if (maintenanceId == null || maintenanceId <= 0) throw new IllegalArgumentException("maintenanceId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

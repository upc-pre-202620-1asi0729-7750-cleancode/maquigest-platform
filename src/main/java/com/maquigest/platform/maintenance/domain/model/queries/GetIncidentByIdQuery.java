package com.maquigest.platform.maintenance.domain.model.queries;
public record GetIncidentByIdQuery(Long incidentId,Long actorUserId) {
    public GetIncidentByIdQuery {
        if (incidentId == null || incidentId <= 0) throw new IllegalArgumentException("incidentId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

package com.maquigest.platform.maintenance.domain.model.queries;
public record GetEquipmentHistoryQuery(Long equipmentId,Long actorUserId) {
    public GetEquipmentHistoryQuery {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

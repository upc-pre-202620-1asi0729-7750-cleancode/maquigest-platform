package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;
public final class IncidentPersistenceAssembler {
    private IncidentPersistenceAssembler(){}
    public static Incident toDomain(IncidentPersistenceEntity e){return new Incident(e.getId(),e.getEquipmentId(),e.getOwnerUserId(),e.getDescription(),e.getReportedAt(),e.isBlocksRental(),e.getStatus(),e.getResolvedAt());}
    public static IncidentPersistenceEntity toNew(Incident i){var e=new IncidentPersistenceEntity();copy(i,e);return e;}
    public static void copy(Incident i,IncidentPersistenceEntity e){
        e.setEquipmentId(i.getEquipmentId());e.setOwnerUserId(i.getOwnerUserId());e.setDescription(i.getDescription());
        e.setReportedAt(i.getReportedAt());e.setBlocksRental(i.isBlocksRental());e.setStatus(i.getStatus());e.setResolvedAt(i.getResolvedAt());
    }
}

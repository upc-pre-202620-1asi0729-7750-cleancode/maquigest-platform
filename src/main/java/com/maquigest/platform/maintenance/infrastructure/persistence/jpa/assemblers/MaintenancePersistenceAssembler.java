package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenancePersistenceEntity;
public final class MaintenancePersistenceAssembler {
    private MaintenancePersistenceAssembler(){}
    public static Maintenance toDomain(MaintenancePersistenceEntity e){return new Maintenance(e.getId(),e.getEquipmentId(),e.getOwnerUserId(),e.getPerformedAt(),e.getType(),e.getStatus(),e.getTechnician(),e.getAcknowledgedAt());}
    public static MaintenancePersistenceEntity toNew(Maintenance m){var e=new MaintenancePersistenceEntity();copy(m,e);return e;}
    public static void copy(Maintenance m,MaintenancePersistenceEntity e){
        e.setEquipmentId(m.getEquipmentId());e.setOwnerUserId(m.getOwnerUserId());e.setPerformedAt(m.getPerformedAt());
        e.setType(m.getType());e.setStatus(m.getStatus());e.setTechnician(m.getTechnician());e.setAcknowledgedAt(m.getAcknowledgedAt());
    }
}

package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenanceActivityPersistenceEntity;
public final class MaintenanceActivityPersistenceAssembler {
    private MaintenanceActivityPersistenceAssembler(){}
    public static MaintenanceActivity toDomain(MaintenanceActivityPersistenceEntity e){return new MaintenanceActivity(e.getId(),e.getMaintenanceId(),e.getStatus(),e.getOccurredAt(),e.getTechnician(),e.getNotes());}
    public static MaintenanceActivityPersistenceEntity toNew(MaintenanceActivity a){var e=new MaintenanceActivityPersistenceEntity();
        e.setMaintenanceId(a.maintenanceId());e.setStatus(a.status());e.setOccurredAt(a.occurredAt());e.setTechnician(a.technician());e.setNotes(a.notes());return e;
    }
}

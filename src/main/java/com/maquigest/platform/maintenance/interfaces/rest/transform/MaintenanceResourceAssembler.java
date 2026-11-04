package com.maquigest.platform.maintenance.interfaces.rest.transform;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.aggregates.*;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.application.queryservices.MaintenanceQueryService;
import com.maquigest.platform.maintenance.interfaces.rest.resources.*;
import java.time.*;
public final class MaintenanceResourceAssembler {
    private MaintenanceResourceAssembler(){}
    public static IncidentResource from(Incident i){
        return new IncidentResource(i.getId(),i.getEquipmentId(),i.getDescription(),i.getReportedAt().toString(),
            i.isBlocksRental(),i.getStatus(),i.getResolvedAt()==null?null:i.getResolvedAt().toString());
    }
    public static IncidentResource publicBlocker(Incident i){
        return new IncidentResource(i.getId(),i.getEquipmentId(),"Equipment unavailable due to an open blocking incident",
            i.getReportedAt().toString(),i.isBlocksRental(),i.getStatus(),null);
    }
    public static MaintenanceResource from(Maintenance m){return new MaintenanceResource(m.getId(),m.getEquipmentId(),
        m.getPerformedAt().toString(),m.getType(),m.getStatus(),m.getTechnician(),
        m.getAcknowledgedAt()==null?null:m.getAcknowledgedAt().toString());}
    public static MaintenanceActivityResource from(MaintenanceActivity a){return new MaintenanceActivityResource(a.id(),
        a.maintenanceId(),a.status(),a.occurredAt().toString(),a.technician(),a.notes());}
    public static EquipmentHistoryResource from(MaintenanceQueryService.EquipmentHistory h){return new EquipmentHistoryResource(
        h.equipmentId(),h.equipmentCode(),h.equipmentName(),h.entries().stream().map(e->new EquipmentHistoryResource.HistoryEntry(
          e.id(),e.kind(),e.occurredAt().toString(),e.description(),e.status())).toList());}
    public static Instant parse(String raw,String field){
        if(raw==null||raw.isBlank())throw MaintenanceException.invalid(field+" is required");
        try{return Instant.parse(raw);}catch(DateTimeException e){throw MaintenanceException.invalid("Invalid "+field+". Use ISO 8601 UTC, e.g. 2026-11-04T12:00:00Z");}
    }
    public static Instant parseOptional(String raw,String field){return raw==null?null:parse(raw,field);}
}

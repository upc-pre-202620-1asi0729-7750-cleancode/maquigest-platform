package com.maquigest.platform.maintenance.application.queryservices;
import com.maquigest.platform.maintenance.domain.model.aggregates.*;
import com.maquigest.platform.maintenance.domain.model.queries.*;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import java.util.*;
public interface MaintenanceQueryService {
    List<Incident> handle(GetIncidentsQuery query);
    Optional<Incident> handle(GetIncidentByIdQuery query);
    List<Maintenance> handle(GetMaintenancesQuery query);
    Optional<Maintenance> handle(GetMaintenanceByIdQuery query);
    List<MaintenanceActivity> activities(Long maintenanceId,Long actorUserId);
    EquipmentHistory handle(GetEquipmentHistoryQuery query);
    record HistoryEntry(Long id,String kind,java.time.Instant occurredAt,String description,String status) { }
    record EquipmentHistory(Long equipmentId,String equipmentCode,String equipmentName,List<HistoryEntry> entries) { }
}

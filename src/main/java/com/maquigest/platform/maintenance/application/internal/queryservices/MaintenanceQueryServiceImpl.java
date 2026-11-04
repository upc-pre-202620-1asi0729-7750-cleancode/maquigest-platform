package com.maquigest.platform.maintenance.application.internal.queryservices;
import com.maquigest.platform.maintenance.application.queryservices.MaintenanceQueryService;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.*;
import com.maquigest.platform.maintenance.domain.model.queries.*;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.repositories.*;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import java.util.*;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class MaintenanceQueryServiceImpl implements MaintenanceQueryService {
    private final IncidentRepository incidents;private final MaintenanceRepository maintenances;
    private final MaintenanceActivityRepository activities;private final MaintenanceInventoryPort inventory;
    private final MaintenanceRentalPort rentals;
    public MaintenanceQueryServiceImpl(IncidentRepository incidents,MaintenanceRepository maintenances,
        MaintenanceActivityRepository activities,MaintenanceInventoryPort inventory,MaintenanceRentalPort rentals){
        this.incidents=incidents;this.maintenances=maintenances;this.activities=activities;
        this.inventory=inventory;this.rentals=rentals;
    }
    @Override public List<Incident> handle(GetIncidentsQuery q){
        if(q.actorUserId()==null||q.actorUserId()<=0)throw MaintenanceException.forbidden();
        if(q.equipmentId()!=null){
            var e=inventory.inspect(q.equipmentId());
            if(e.ownerUserId().equals(q.actorUserId())&&q.rentalCompany())return incidents.findByEquipmentId(q.equipmentId());
            // Construction companies may inspect OPEN blocking restrictions, but not private incident narratives.
            if(!q.rentalCompany())return incidents.findByEquipmentId(q.equipmentId()).stream()
                .filter(i->i.getStatus()==IncidentStatus.OPEN&&i.isBlocksRental()).toList();
            throw MaintenanceException.forbidden();
        }
        if(!q.rentalCompany())throw MaintenanceException.forbidden();
        return incidents.findByOwner(q.actorUserId());
    }
    @Override public Optional<Incident> handle(GetIncidentByIdQuery q){
        return incidents.findById(q.incidentId()).filter(i->i.getOwnerUserId().equals(q.actorUserId()));
    }
    @Override public List<Maintenance> handle(GetMaintenancesQuery q){
        if(q.equipmentId()!=null){var e=inventory.inspect(q.equipmentId());
            if(!e.ownerUserId().equals(q.actorUserId()))throw MaintenanceException.forbidden();
            return maintenances.findByEquipmentId(q.equipmentId());}
        return maintenances.findByOwner(q.actorUserId());
    }
    @Override public Optional<Maintenance> handle(GetMaintenanceByIdQuery q){
        return maintenances.findById(q.maintenanceId()).filter(m->m.getOwnerUserId().equals(q.actorUserId()));
    }
    @Override public List<MaintenanceActivity> activities(Long maintenanceId,Long actorUserId){
        if(handle(new GetMaintenanceByIdQuery(maintenanceId,actorUserId)).isEmpty())throw MaintenanceException.notFound("Maintenance");
        return activities.findByMaintenanceId(maintenanceId);
    }
    private Instant maintenanceHistoryTime(Maintenance maintenance) {
        if(maintenance.getStatus()==MaintenanceStatus.SCHEDULED)
            return maintenance.getPerformedAt();
        // For an updated maintenance, use the real status-change activity.
        // A maintenance registered directly as COMPLETED uses its performedAt date.
        return activities.findByMaintenanceId(maintenance.getId()).stream()
            .filter(a->a.status()==maintenance.getStatus())
            .filter(a->!"Registered maintenance".equals(a.notes()))
            .map(MaintenanceActivity::occurredAt)
            .max(Comparator.naturalOrder())
            .orElse(maintenance.getPerformedAt());
    }
    @Override public EquipmentHistory handle(GetEquipmentHistoryQuery q){
        if(q.equipmentId()==null||q.equipmentId()<=0)throw MaintenanceException.invalid("Invalid equipment id");
        var e=inventory.inspect(q.equipmentId());
        if(!e.ownerUserId().equals(q.actorUserId()))throw MaintenanceException.forbidden();
        List<HistoryEntry> entries=new ArrayList<>();
        incidents.findByEquipmentId(e.id()).forEach(i->entries.add(new HistoryEntry(i.getId(),"INCIDENT",
            i.getReportedAt(),i.getDescription(),i.getStatus().name())));
        maintenances.findByEquipmentId(e.id()).forEach(m->entries.add(new HistoryEntry(m.getId(),"MAINTENANCE",
            maintenanceHistoryTime(m),m.getType(),m.getStatus().name())));
        rentals.history(e.id(),q.actorUserId()).forEach(r->entries.add(new HistoryEntry(r.id(),"RENTAL",
            r.occurredAt(),r.description(),r.status())));
        entries.sort(Comparator.comparing(HistoryEntry::occurredAt).reversed());
        return new EquipmentHistory(e.id(),e.code(),e.name(),List.copyOf(entries));
    }
}

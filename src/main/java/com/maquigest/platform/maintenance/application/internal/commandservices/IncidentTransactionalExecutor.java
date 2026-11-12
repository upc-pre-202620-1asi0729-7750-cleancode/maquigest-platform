package com.maquigest.platform.maintenance.application.internal.commandservices;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.*;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import com.maquigest.platform.maintenance.domain.repositories.*;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class IncidentTransactionalExecutor {
    private final IncidentRepository incidents;
    private final MaintenanceRepository maintenances;
    private final MaintenanceActivityRepository activities;
    private final MaintenanceInventoryPort inventory;
    private final MaintenanceRentalPort rentals;
    public IncidentTransactionalExecutor(IncidentRepository incidents,MaintenanceRepository maintenances,
                                      MaintenanceActivityRepository activities, MaintenanceInventoryPort inventory,
                                      MaintenanceRentalPort rentals){
        this.incidents=incidents;this.maintenances=maintenances;this.activities=activities;
        this.inventory=inventory;this.rentals=rentals;
    }
    private MaintenanceInventoryPort.EquipmentFacts ownedAndLocked(Long equipmentId,Long actor){
        if(equipmentId==null||equipmentId<=0||actor==null||actor<=0)throw MaintenanceException.invalid("Invalid equipment or actor");
        var facts=inventory.lock(equipmentId);
        if(!actor.equals(facts.ownerUserId()))throw MaintenanceException.forbidden();
        return facts;
    }
    public Incident handle(ReportIncidentCommand command,Long actor){
        if(command==null)throw MaintenanceException.invalid("Request body is required");
        var facts=ownedAndLocked(command.equipmentId(),actor);
        if(command.blocksRental()) {
            if(rentals.hasActiveRental(facts.id(),actor))throw MaintenanceException.conflict("Equipment has an active rental");
            inventory.markMaintenance(facts.id(),actor);
        }
        try {return incidents.save(Incident.report(facts.id(),actor,command.description(),command.blocksRental(),Instant.now()));}
        catch(IllegalArgumentException ex){throw MaintenanceException.invalid(ex.getMessage());}
    }
    public Incident handle(PatchIncidentCommand command,Long actor){
        if(command==null||command.incidentId()==null||command.incidentId()<=0)throw MaintenanceException.invalid("Invalid incident id");
        // Lock ordering is Inventory -> Incident for every operation involving both contexts.
        var original=incidents.findById(command.incidentId()).orElseThrow(()->MaintenanceException.notFound("Incident"));
        var facts=ownedAndLocked(original.getEquipmentId(),actor);
        var incident=incidents.findByIdForUpdate(command.incidentId()).orElseThrow(()->MaintenanceException.notFound("Incident"));
        if(!incident.getOwnerUserId().equals(actor)||!incident.getEquipmentId().equals(facts.id()))throw MaintenanceException.forbidden();
        boolean restrict=Boolean.TRUE.equals(command.blocksRental());
        boolean resolve=command.status()==IncidentStatus.RESOLVED;
        if(restrict==resolve)throw MaintenanceException.invalid("Choose exactly one action: blocksRental=true or status=RESOLVED");
        if(restrict){
            if(rentals.hasActiveRental(facts.id(),actor))throw MaintenanceException.conflict("Equipment has an active rental");
            try{incident.requireRestriction();}catch(IllegalStateException ex){throw MaintenanceException.conflict(ex.getMessage());}
            inventory.markMaintenance(facts.id(),actor);
        }else{
            if(command.blocksRental()!=null)throw MaintenanceException.invalid("Cannot change rental restriction when resolving");
            try{incident.resolve(command.resolvedAt()==null?Instant.now():command.resolvedAt());}
            catch(IllegalStateException ex){throw MaintenanceException.conflict(ex.getMessage());}
            catch(IllegalArgumentException ex){throw MaintenanceException.invalid(ex.getMessage());}
        }
        return incidents.save(incident);
    }
    public void handle(ReactivateEquipmentCommand command,Long actor){
        if(command==null||!command.inspectionConfirmed())throw MaintenanceException.invalid("Technical inspection confirmation is required");
        var facts=ownedAndLocked(command.equipmentId(),actor);
        if(!"MAINTENANCE".equals(facts.status()))throw MaintenanceException.conflict("Only MAINTENANCE equipment can be reactivated");
        // Preserve the original resolved-blocking-incident path.
        // A return with maintenance required is an additional, independently verified path.
        if(!incidents.hadBlocking(facts.id())) {
            var latestReturn=rentals.latestReturn(facts.id(),actor);
            if(latestReturn.isEmpty() || !latestReturn.get().maintenanceRequired())
                throw MaintenanceException.conflict("Blocking incident history or maintenance-required return is required");
            var completed=maintenances.findByEquipmentId(facts.id()).stream()
                .filter(m->m.getOwnerUserId().equals(actor) && m.getStatus()==MaintenanceStatus.COMPLETED)
                .anyMatch(m->activities.findByMaintenanceId(m.getId()).stream().anyMatch(activity->
                    activity.status()==MaintenanceStatus.COMPLETED &&
                    !activity.occurredAt().isBefore(latestReturn.get().returnedAt())));
            if(!completed)throw MaintenanceException.conflict("A completed intervention after the maintenance-required return is required");
            if(maintenances.findByEquipmentId(facts.id()).stream().anyMatch(m->
                m.getStatus()==MaintenanceStatus.SCHEDULED))
                throw MaintenanceException.conflict("Scheduled maintenance must be completed before reactivation");
        }
        if(incidents.hasOpenBlocking(facts.id()))throw MaintenanceException.conflict("Open blocking incident prevents reactivation");
        if(rentals.hasActiveRental(facts.id(),actor))throw MaintenanceException.conflict("Active rental prevents reactivation");
        if(maintenances.findByEquipmentId(facts.id()).stream().anyMatch(m->m.getStatus()==MaintenanceStatus.IN_PROGRESS))
            throw MaintenanceException.conflict("Maintenance is still in progress");
        inventory.reactivate(facts.id(),actor);
    }
}

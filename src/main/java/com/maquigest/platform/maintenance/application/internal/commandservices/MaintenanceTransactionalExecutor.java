package com.maquigest.platform.maintenance.application.internal.commandservices;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import com.maquigest.platform.maintenance.domain.repositories.*;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class MaintenanceTransactionalExecutor {
    private final MaintenanceRepository maintenances;
    private final MaintenanceActivityRepository activities;
    private final MaintenanceInventoryPort inventory;
    private final MaintenanceRentalPort rentals;
    private final MaintenanceSubscriptionPort subscriptions;
    public MaintenanceTransactionalExecutor(MaintenanceRepository maintenances,MaintenanceActivityRepository activities,
        MaintenanceInventoryPort inventory,MaintenanceRentalPort rentals,MaintenanceSubscriptionPort subscriptions){
        this.maintenances=maintenances;this.activities=activities;
        this.inventory=inventory;this.rentals=rentals;this.subscriptions=subscriptions;
    }
    public Maintenance handle(RegisterMaintenanceCommand command,Long actor){
        if(command==null)throw MaintenanceException.invalid("Maintenance body is required");
        var equipment=inventory.lock(command.equipmentId());
        if(actor==null||!actor.equals(equipment.ownerUserId()))throw MaintenanceException.forbidden();
        if(!subscriptions.hasActiveGrowth(actor))throw MaintenanceException.forbidden();
        Maintenance result;
        try{result=Maintenance.register(equipment.id(),actor,command.performedAt(),command.type(),command.status());}
        catch(IllegalArgumentException ex){throw MaintenanceException.invalid(ex.getMessage());}
        result=maintenances.save(result);
        activities.save(new MaintenanceActivity(null,result.getId(),result.getStatus(),Instant.now(),"","Registered maintenance"));
        return result;
    }
    public Maintenance handle(AdvanceMaintenanceCommand command,Long actor){
        if(command==null||command.maintenanceId()==null||command.maintenanceId()<=0)
            throw MaintenanceException.invalid("Invalid maintenance id");
        // Lock order: Inventory -> Maintenance, consistent with other operations.
        var existing=maintenances.findById(command.maintenanceId()).orElseThrow(()->MaintenanceException.notFound("Maintenance"));
        var equipment=inventory.lock(existing.getEquipmentId());
        if(actor==null||!actor.equals(equipment.ownerUserId())||!existing.getOwnerUserId().equals(actor))throw MaintenanceException.forbidden();
        if(!subscriptions.hasActiveGrowth(actor))throw MaintenanceException.forbidden();
        var maintenance=maintenances.findByIdForUpdate(command.maintenanceId()).orElseThrow(()->MaintenanceException.notFound("Maintenance"));
        if(command.technician()!=null&&!command.technician().isBlank()){
            try{maintenance.assign(command.technician());}catch(IllegalArgumentException ex){throw MaintenanceException.invalid(ex.getMessage());}
        }
        if(command.acknowledge()) {
            try{maintenance.acknowledge(Instant.now());}
            catch(IllegalStateException ex){throw MaintenanceException.conflict(ex.getMessage());}
        }
        if(command.status()!=null&&command.status()!=maintenance.getStatus()){
            if(command.status()==MaintenanceStatus.IN_PROGRESS &&
               rentals.hasActiveRental(equipment.id(),actor))
                throw MaintenanceException.conflict("Equipment has an active rental");

            try{
                maintenance.changeStatus(
                    command.status(),
                    command.technician(),
                    command.notes(),
                    Instant.now()
                );
            }
            catch(IllegalStateException ex){
                throw MaintenanceException.conflict(ex.getMessage());
            }

            if(command.status()==MaintenanceStatus.IN_PROGRESS)
                inventory.markMaintenance(equipment.id(),actor);
        }else if((command.technician()==null||command.technician().isBlank())&&!command.acknowledge()){
            throw MaintenanceException.invalid("No maintenance changes were requested");
        }
        var saved=maintenances.save(maintenance);
        activities.save(new MaintenanceActivity(null,saved.getId(),saved.getStatus(),Instant.now(),saved.getTechnician(),command.notes()));
        return saved;
    }
}

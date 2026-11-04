package com.maquigest.platform.maintenance.infrastructure.acl.inventory;
import com.maquigest.platform.maintenance.application.ports.MaintenanceInventoryPort;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import org.springframework.stereotype.Component;
@Component
public class InventoryMaintenanceAclAdapter implements MaintenanceInventoryPort {
    private final EquipmentRepository inventory;
    public InventoryMaintenanceAclAdapter(EquipmentRepository inventory){this.inventory=inventory;}
    private static EquipmentFacts map(Equipment e){return new EquipmentFacts(e.getId(),e.getUserId(),e.getCode(),e.getName(),e.getStatus().name());}
    @Override public EquipmentFacts inspect(Long id){
        if(id==null||id<=0)throw MaintenanceException.invalid("Invalid equipment id");
        return map(inventory.findById(id).orElseThrow(()->MaintenanceException.notFound("Equipment")));
    }
    @Override public EquipmentFacts lock(Long id){
        if(id==null||id<=0)throw MaintenanceException.invalid("Invalid equipment id");
        return map(inventory.findByIdForUpdate(id).orElseThrow(()->MaintenanceException.notFound("Equipment")));
    }
    private Equipment owned(Long id,Long owner){
        var e=inventory.findByIdForUpdate(id).orElseThrow(()->MaintenanceException.notFound("Equipment"));
        if(!e.getUserId().equals(owner))throw MaintenanceException.forbidden();
        return e;
    }
    @Override public void markMaintenance(Long id,Long owner){
        var e=owned(id,owner);
        if(e.getStatus()==EquipmentStatus.RENTED)throw MaintenanceException.conflict("Rented equipment cannot enter maintenance directly");
        if(e.getStatus()==EquipmentStatus.MAINTENANCE)return;
        try{e.changeOperationalStatus(EquipmentStatus.MAINTENANCE);}
        catch(IllegalStateException ex){throw MaintenanceException.conflict(ex.getMessage());}
        inventory.save(e);
    }
    @Override public void reactivate(Long id,Long owner){
        var e=owned(id,owner);
        if(e.getStatus()!=EquipmentStatus.MAINTENANCE)throw MaintenanceException.conflict("Equipment must be in MAINTENANCE");
        try{e.changeOperationalStatus(EquipmentStatus.AVAILABLE);}
        catch(IllegalStateException ex){throw MaintenanceException.conflict(ex.getMessage());}
        inventory.save(e);
    }
}

package com.maquigest.platform.maintenance.infrastructure.acl.inventory;

import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFailure;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.ports.MaintenanceInventoryPort;
import org.springframework.stereotype.Component;

/** Anti-corruption adapter: Maintenance sees its own facts and error vocabulary only. */
@Component
public class InventoryMaintenanceAclAdapter implements MaintenanceInventoryPort {
    private final InventoryMaintenanceFacade inventory;

    public InventoryMaintenanceAclAdapter(InventoryMaintenanceFacade inventory) {
        this.inventory = inventory;
    }

    private static EquipmentFacts map(InventoryMaintenanceFacade.EquipmentSnapshot item) {
        return new EquipmentFacts(item.id(), item.ownerUserId(), item.code(), item.name(), item.status());
    }

    private static MaintenanceException translate(InventoryMaintenanceFailure failure) {
        return switch (failure.kind()) {
            case INVALID -> MaintenanceException.invalid(failure.getMessage());
            case NOT_FOUND -> MaintenanceException.notFound("Equipment");
            case FORBIDDEN -> MaintenanceException.forbidden();
            case CONFLICT -> MaintenanceException.conflict(failure.getMessage());
        };
    }

    @Override
    public EquipmentFacts inspect(Long equipmentId) {
        try { return map(inventory.inspect(equipmentId)); }
        catch (InventoryMaintenanceFailure failure) { throw translate(failure); }
    }

    @Override
    public EquipmentFacts lock(Long equipmentId) {
        try { return map(inventory.lock(equipmentId)); }
        catch (InventoryMaintenanceFailure failure) { throw translate(failure); }
    }

    @Override
    public void markMaintenance(Long equipmentId, Long ownerUserId) {
        try { inventory.markMaintenance(equipmentId, ownerUserId); }
        catch (InventoryMaintenanceFailure failure) { throw translate(failure); }
    }

    @Override
    public void reactivate(Long equipmentId, Long ownerUserId) {
        try { inventory.reactivate(equipmentId, ownerUserId); }
        catch (InventoryMaintenanceFailure failure) { throw translate(failure); }
    }
}

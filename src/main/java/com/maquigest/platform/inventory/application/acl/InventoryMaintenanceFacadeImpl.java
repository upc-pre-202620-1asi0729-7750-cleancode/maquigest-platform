package com.maquigest.platform.inventory.application.acl;

import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFailure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Inventory alone owns Equipment domain mutations and its exclusive InnoDB row lock. */
@Service
public class InventoryMaintenanceFacadeImpl implements InventoryMaintenanceFacade {
    private final EquipmentRepository inventory;

    public InventoryMaintenanceFacadeImpl(EquipmentRepository inventory) {
        this.inventory = inventory;
    }

    private static void validId(Long id) {
        if (id == null || id <= 0) throw InventoryMaintenanceFailure.invalid("Invalid equipment id");
    }

    private static EquipmentSnapshot snapshot(Equipment e) {
        return new EquipmentSnapshot(e.getId(), e.getUserId(), e.getCode(), e.getName(), e.getStatus().name());
    }

    private Equipment lockedOwned(Long equipmentId, Long ownerUserId) {
        // Keep lock acquisition before ownership validation, as in the original adapter.
        var equipment = inventory.findByIdForUpdate(equipmentId)
            .orElseThrow(InventoryMaintenanceFailure::notFound);
        if (!equipment.getUserId().equals(ownerUserId)) throw InventoryMaintenanceFailure.forbidden();
        return equipment;
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentSnapshot inspect(Long equipmentId) {
        validId(equipmentId);
        return snapshot(inventory.findById(equipmentId).orElseThrow(InventoryMaintenanceFailure::notFound));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public EquipmentSnapshot lock(Long equipmentId) {
        validId(equipmentId);
        return snapshot(inventory.findByIdForUpdate(equipmentId).orElseThrow(InventoryMaintenanceFailure::notFound));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void markMaintenance(Long equipmentId, Long ownerUserId) {
        var equipment = lockedOwned(equipmentId, ownerUserId);
        if (equipment.getStatus() == EquipmentStatus.RENTED)
            throw InventoryMaintenanceFailure.conflict("Rented equipment cannot enter maintenance directly");
        if (equipment.getStatus() == EquipmentStatus.MAINTENANCE) return;
        try { equipment.changeOperationalStatus(EquipmentStatus.MAINTENANCE); }
        catch (IllegalStateException ex) { throw InventoryMaintenanceFailure.conflict(ex.getMessage()); }
        inventory.save(equipment);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void reactivate(Long equipmentId, Long ownerUserId) {
        var equipment = lockedOwned(equipmentId, ownerUserId);
        if (equipment.getStatus() != EquipmentStatus.MAINTENANCE)
            throw InventoryMaintenanceFailure.conflict("Equipment must be in MAINTENANCE");
        try { equipment.changeOperationalStatus(EquipmentStatus.AVAILABLE); }
        catch (IllegalStateException ex) { throw InventoryMaintenanceFailure.conflict(ex.getMessage()); }
        inventory.save(equipment);
    }
}

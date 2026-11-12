package com.maquigest.platform.inventory.interfaces.acl;

/** Published Inventory contract consumed by Maintenance; no aggregate or JPA types leak out. */
public interface InventoryMaintenanceFacade {
    record EquipmentSnapshot(Long id, Long ownerUserId, String code, String name, String status) { }

    EquipmentSnapshot inspect(Long equipmentId);
    /** All mutating and lock operations require the calling Maintenance use-case transaction. */
    EquipmentSnapshot lock(Long equipmentId);
    void markMaintenance(Long equipmentId, Long ownerUserId);
    void reactivate(Long equipmentId, Long ownerUserId);
}

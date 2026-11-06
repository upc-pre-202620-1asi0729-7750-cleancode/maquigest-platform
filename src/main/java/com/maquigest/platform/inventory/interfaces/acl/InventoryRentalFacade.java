package com.maquigest.platform.inventory.interfaces.acl;

import java.time.LocalDate;

/** Published Inventory contract for Rentals. Never returns Inventory aggregates or JPA objects. */
public interface InventoryRentalFacade {
    record EquipmentSnapshot(Long id, Long ownerUserId, boolean availableForPeriod) { }

    EquipmentSnapshot inspect(Long equipmentId, LocalDate from, LocalDate to);
    /** Requires existing transaction so Inventory and Rentals commit/roll back atomically. */
    Long reserve(Long equipmentId, Long ownerUserId, LocalDate from, LocalDate to);
    void markDelivered(Long equipmentId, Long ownerUserId, Long blockId);
    void completeReturn(Long equipmentId, Long ownerUserId, Long blockId, boolean maintenanceRequired);
}

package com.maquigest.platform.rentals.application.ports;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
public interface RentalEquipmentPort {
    record EquipmentFacts(Long id, Long ownerUserId, boolean availableForPeriod) { }
    EquipmentFacts inspect(Long equipmentId, RentalPeriod period);
    /** Atomically reserve Inventory and return the generated availability block identifier. */
    Long reserve(Long equipmentId, Long ownerUserId, RentalPeriod period);
    void markDelivered(Long equipmentId, Long ownerUserId, Long blockId);
    void completeReturn(Long equipmentId, Long ownerUserId, Long blockId, boolean maintenanceRequired);
}

package com.maquigest.platform.maintenance.application.ports;
public interface MaintenanceInventoryPort {
    record EquipmentFacts(Long id,Long ownerUserId,String code,String name,String status) { }
    EquipmentFacts inspect(Long equipmentId);
    EquipmentFacts lock(Long equipmentId);
    void markMaintenance(Long equipmentId,Long ownerUserId);
    void reactivate(Long equipmentId,Long ownerUserId);
}

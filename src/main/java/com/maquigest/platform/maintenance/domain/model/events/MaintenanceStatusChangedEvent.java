package com.maquigest.platform.maintenance.domain.model.events;
public record MaintenanceStatusChangedEvent(Long maintenanceId,Long equipmentId,String status) {

}

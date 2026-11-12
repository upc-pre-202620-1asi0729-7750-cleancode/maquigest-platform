package com.maquigest.platform.maintenance.interfaces.rest.resources;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
public record MaintenanceResource(Long id,Long equipmentId,String performedAt,String type,
    MaintenanceStatus status,String technician,String acknowledgedAt) { }

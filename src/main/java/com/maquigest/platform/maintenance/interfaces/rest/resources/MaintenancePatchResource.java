package com.maquigest.platform.maintenance.interfaces.rest.resources;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
public record MaintenancePatchResource(MaintenanceStatus status,String technician,Boolean acknowledge,String notes) { }

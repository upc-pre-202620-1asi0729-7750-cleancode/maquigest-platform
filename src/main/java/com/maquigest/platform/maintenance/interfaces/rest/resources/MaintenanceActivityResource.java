package com.maquigest.platform.maintenance.interfaces.rest.resources;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
public record MaintenanceActivityResource(Long id,Long maintenanceId,MaintenanceStatus status,String occurredAt,
                                           String technician,String notes) { }

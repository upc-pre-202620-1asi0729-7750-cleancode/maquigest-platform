package com.maquigest.platform.maintenance.interfaces.rest.resources;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
public record IncidentResource(Long id,Long equipmentId,String description,String reportedAt,
                               Boolean blocksRental,IncidentStatus status,String resolvedAt) { }

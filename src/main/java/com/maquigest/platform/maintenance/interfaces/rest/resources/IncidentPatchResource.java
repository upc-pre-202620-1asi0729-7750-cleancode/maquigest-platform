package com.maquigest.platform.maintenance.interfaces.rest.resources;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
public record IncidentPatchResource(Boolean blocksRental,IncidentStatus status,String resolvedAt) { }

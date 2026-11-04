package com.maquigest.platform.maintenance.domain.model.commands;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
public record PatchIncidentCommand(Long incidentId,Boolean blocksRental,IncidentStatus status,Instant resolvedAt) {

}

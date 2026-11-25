package com.maquigest.platform.maintenance.domain.model.commands;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
public record PatchIncidentCommand(Long incidentId,Boolean blocksRental,IncidentStatus status,Instant resolvedAt) {
    public PatchIncidentCommand {
        if (incidentId == null || incidentId <= 0) throw new IllegalArgumentException("incidentId must be positive");
    }
}

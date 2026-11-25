package com.maquigest.platform.maintenance.domain.model.commands;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
public record ReportIncidentCommand(Long equipmentId,String description,boolean blocksRental) {
    public ReportIncidentCommand {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
        if (description == null || description.isBlank() || description.trim().length() > 3000) throw new IllegalArgumentException("description is required (max 3000)");
    }
}

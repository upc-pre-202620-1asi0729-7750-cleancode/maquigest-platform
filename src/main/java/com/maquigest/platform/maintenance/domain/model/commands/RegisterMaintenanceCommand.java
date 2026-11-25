package com.maquigest.platform.maintenance.domain.model.commands;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
public record RegisterMaintenanceCommand(Long equipmentId,Instant performedAt,String type,MaintenanceStatus status) {
    public RegisterMaintenanceCommand {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
        if (performedAt == null) throw new IllegalArgumentException("performedAt is required");
        new com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceType(type);
        if (status == null) throw new IllegalArgumentException("status is required");
    }
}

package com.maquigest.platform.maintenance.domain.model.commands;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
public record ReactivateEquipmentCommand(Long equipmentId,boolean inspectionConfirmed) {
    public ReactivateEquipmentCommand {
        if (equipmentId == null || equipmentId <= 0) throw new IllegalArgumentException("equipmentId must be positive");
    }
}

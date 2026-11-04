package com.maquigest.platform.maintenance.domain.model.commands;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
public record ReportIncidentCommand(Long equipmentId,String description,boolean blocksRental) {

}

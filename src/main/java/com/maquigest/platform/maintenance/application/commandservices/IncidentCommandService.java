package com.maquigest.platform.maintenance.application.commandservices;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
public interface IncidentCommandService {
    Incident handle(ReportIncidentCommand command,Long actorUserId);
    Incident handle(PatchIncidentCommand command,Long actorUserId);
    void handle(ReactivateEquipmentCommand command,Long actorUserId);
}

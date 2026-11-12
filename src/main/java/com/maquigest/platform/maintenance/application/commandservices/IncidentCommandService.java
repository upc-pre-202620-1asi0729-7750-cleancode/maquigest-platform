package com.maquigest.platform.maintenance.application.commandservices;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.shared.application.result.ApplicationError;
public interface IncidentCommandService {
    Result<Incident, ApplicationError> handle(ReportIncidentCommand command,Long actorUserId);
    Result<Incident, ApplicationError> handle(PatchIncidentCommand command,Long actorUserId);
    Result<Boolean, ApplicationError> handle(ReactivateEquipmentCommand command,Long actorUserId);
}

package com.maquigest.platform.maintenance.application.commandservices;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.shared.application.result.ApplicationError;
public interface MaintenanceCommandService {
    Result<Maintenance, ApplicationError> handle(RegisterMaintenanceCommand command,Long actorUserId);
    Result<Maintenance, ApplicationError> handle(AdvanceMaintenanceCommand command,Long actorUserId);
}

package com.maquigest.platform.maintenance.application.commandservices;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
public interface MaintenanceCommandService {
    Maintenance handle(RegisterMaintenanceCommand command,Long actorUserId);
    Maintenance handle(AdvanceMaintenanceCommand command,Long actorUserId);
}

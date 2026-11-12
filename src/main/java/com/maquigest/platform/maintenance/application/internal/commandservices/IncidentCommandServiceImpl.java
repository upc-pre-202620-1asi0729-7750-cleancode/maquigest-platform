package com.maquigest.platform.maintenance.application.internal.commandservices;

import com.maquigest.platform.maintenance.application.commandservices.IncidentCommandService;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/** Application Result boundary OUTSIDE the transactional executor. */
@Service
public class IncidentCommandServiceImpl implements IncidentCommandService {
    private final IncidentTransactionalExecutor executor;

    public IncidentCommandServiceImpl(IncidentTransactionalExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Result<Incident, ApplicationError> handle(ReportIncidentCommand command, Long actorUserId) {
        try {
            return Result.success(executor.handle(command, actorUserId));
        } catch (MaintenanceException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }

    @Override
    public Result<Incident, ApplicationError> handle(PatchIncidentCommand command, Long actorUserId) {
        try {
            return Result.success(executor.handle(command, actorUserId));
        } catch (MaintenanceException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }

    @Override
    public Result<Boolean, ApplicationError> handle(ReactivateEquipmentCommand command, Long actorUserId) {
        try {
            executor.handle(command, actorUserId);
            return Result.success(Boolean.TRUE);
        } catch (MaintenanceException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }
}

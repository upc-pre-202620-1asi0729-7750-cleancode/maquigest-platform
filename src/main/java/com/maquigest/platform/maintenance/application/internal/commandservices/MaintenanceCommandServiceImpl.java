package com.maquigest.platform.maintenance.application.internal.commandservices;

import com.maquigest.platform.maintenance.application.commandservices.MaintenanceCommandService;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/** Application Result boundary OUTSIDE the transactional executor. */
@Service
public class MaintenanceCommandServiceImpl implements MaintenanceCommandService {
    private final MaintenanceTransactionalExecutor executor;

    public MaintenanceCommandServiceImpl(MaintenanceTransactionalExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Result<Maintenance, ApplicationError> handle(RegisterMaintenanceCommand command, Long actorUserId) {
        try {
            return Result.success(executor.handle(command, actorUserId));
        } catch (MaintenanceException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }

    @Override
    public Result<Maintenance, ApplicationError> handle(AdvanceMaintenanceCommand command, Long actorUserId) {
        try {
            return Result.success(executor.handle(command, actorUserId));
        } catch (MaintenanceException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }
}

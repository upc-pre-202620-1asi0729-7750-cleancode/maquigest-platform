package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.application.commandservices.RentalRequestCommandService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/** Application Result boundary OUTSIDE the transactional executor. */
@Service
public class RentalRequestCommandServiceImpl implements RentalRequestCommandService {
    private final RentalRequestTransactionalExecutor executor;

    public RentalRequestCommandServiceImpl(RentalRequestTransactionalExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Result<RentalRequest, ApplicationError> handle(CreateRentalRequestCommand command) {
        try {
            return Result.success(executor.handle(command));
        } catch (RentalsException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }

    @Override
    public Result<RentalRequest, ApplicationError> handle(ResolveRentalRequestCommand command) {
        try {
            return Result.success(executor.handle(command));
        } catch (RentalsException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }

    @Override
    public Result<RentalRequest, ApplicationError> handle(RescheduleRentalRequestCommand command) {
        try {
            return Result.success(executor.handle(command));
        } catch (RentalsException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }
}

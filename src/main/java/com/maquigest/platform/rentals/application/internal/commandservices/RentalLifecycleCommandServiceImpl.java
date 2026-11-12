package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.application.commandservices.RentalLifecycleCommandService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/** Application Result boundary OUTSIDE the transactional executor. */
@Service
public class RentalLifecycleCommandServiceImpl implements RentalLifecycleCommandService {
    private final RentalLifecycleTransactionalExecutor executor;

    public RentalLifecycleCommandServiceImpl(RentalLifecycleTransactionalExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Result<Delivery, ApplicationError> handle(RegisterDeliveryCommand command) {
        try {
            return Result.success(executor.handle(command));
        } catch (RentalsException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }

    @Override
    public Result<EquipmentReturn, ApplicationError> handle(RegisterReturnCommand command) {
        try {
            return Result.success(executor.handle(command));
        } catch (RentalsException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }
}

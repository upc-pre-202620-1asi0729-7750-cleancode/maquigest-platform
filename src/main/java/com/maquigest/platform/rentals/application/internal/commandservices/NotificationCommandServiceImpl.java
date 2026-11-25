package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.application.commandservices.NotificationCommandService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.domain.model.commands.MarkNotificationAsReadCommand;
import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/** Result boundary outside the transactional executor, consistent with Rentals. */
@Service("rentalNotificationCommandServiceImpl")
public class NotificationCommandServiceImpl implements NotificationCommandService {
    private final NotificationTransactionalExecutor executor;

    public NotificationCommandServiceImpl(NotificationTransactionalExecutor executor) {
        this.executor = executor;
    }

    @Override
    public Result<Long, ApplicationError> handle(CreateNotificationCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.validationError("Notification", "validation.notification.required"));
        }
        return Result.success(executor.handle(command));
    }

    @Override
    public Result<Notification, ApplicationError> handle(MarkNotificationAsReadCommand command) {
        if (command == null) {
            return Result.failure(ApplicationError.validationError("Notification", "validation.notification.required"));
        }
        try {
            return Result.success(executor.handle(command));
        } catch (RentalsException failure) {
            return Result.failure(new ApplicationError(failure.code(), failure.getMessage()));
        }
    }
}

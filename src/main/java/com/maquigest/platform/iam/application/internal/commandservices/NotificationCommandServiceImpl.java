package com.maquigest.platform.iam.application.internal.commandservices;

import com.maquigest.platform.iam.application.commandservices.NotificationCommandService;
import com.maquigest.platform.iam.domain.model.aggregates.Notification;
import com.maquigest.platform.iam.domain.model.commands.MarkNotificationAsReadCommand;
import com.maquigest.platform.iam.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/** Result boundary outside the transactional executor, consistent with Iam. */
@Service("iamNotificationCommandServiceImpl")
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
        return executor.handle(command);
    }
}

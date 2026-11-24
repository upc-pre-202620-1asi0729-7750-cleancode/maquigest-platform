package com.maquigest.platform.rentals.application.commandservices;

import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;

public interface NotificationCommandService {
    Result<Long, ApplicationError> handle(CreateNotificationCommand command);
}

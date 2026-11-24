package com.maquigest.platform.inventory.application.commandservices;

import com.maquigest.platform.inventory.domain.model.aggregates.Notification;
import com.maquigest.platform.inventory.domain.model.commands.MarkNotificationAsReadCommand;
import com.maquigest.platform.inventory.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;

public interface NotificationCommandService {
    Result<Long, ApplicationError> handle(CreateNotificationCommand command);
    Result<Notification, ApplicationError> handle(MarkNotificationAsReadCommand command);
}

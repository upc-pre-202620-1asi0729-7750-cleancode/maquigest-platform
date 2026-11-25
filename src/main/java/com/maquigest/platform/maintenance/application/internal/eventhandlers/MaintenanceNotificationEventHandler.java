package com.maquigest.platform.maintenance.application.internal.eventhandlers;

import com.maquigest.platform.maintenance.application.commandservices.NotificationCommandService;
import com.maquigest.platform.maintenance.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.maintenance.domain.model.events.MaintenanceNotificationRequestedEvent;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MaintenanceNotificationEventHandler {
    private final NotificationCommandService commands;
    public MaintenanceNotificationEventHandler(NotificationCommandService commands) { this.commands = commands; }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(MaintenanceNotificationRequestedEvent event) {
        commands.handle(new CreateNotificationCommand(new NotificationRecipientId(event.userId()),
                new NotificationReferenceId(event.referenceId()), event.type(), event.occurredAt(), event.factId()))
                .toOptional().orElseThrow();
    }
}

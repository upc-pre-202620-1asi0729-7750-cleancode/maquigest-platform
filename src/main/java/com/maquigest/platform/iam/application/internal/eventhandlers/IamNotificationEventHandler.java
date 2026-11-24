package com.maquigest.platform.iam.application.internal.eventhandlers;

import com.maquigest.platform.iam.application.commandservices.NotificationCommandService;
import com.maquigest.platform.iam.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.iam.domain.model.events.IamNotificationRequestedEvent;
import com.maquigest.platform.iam.domain.model.valueobjects.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class IamNotificationEventHandler {
    private final NotificationCommandService commands;
    public IamNotificationEventHandler(NotificationCommandService commands) { this.commands = commands; }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(IamNotificationRequestedEvent event) {
        commands.handle(new CreateNotificationCommand(new NotificationRecipientId(event.userId()),
                new NotificationReferenceId(event.referenceId()), event.type(), event.occurredAt(), event.factId()))
                .toOptional().orElseThrow();
    }
}

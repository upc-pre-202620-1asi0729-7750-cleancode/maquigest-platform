package com.maquigest.platform.subscriptions.application.internal.eventhandlers;

import com.maquigest.platform.subscriptions.application.commandservices.NotificationCommandService;
import com.maquigest.platform.subscriptions.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.subscriptions.domain.model.events.SubscriptionsNotificationRequestedEvent;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class SubscriptionsNotificationEventHandler {
    private final NotificationCommandService commands;
    public SubscriptionsNotificationEventHandler(NotificationCommandService commands) { this.commands = commands; }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(SubscriptionsNotificationRequestedEvent event) {
        commands.handle(new CreateNotificationCommand(new NotificationRecipientId(event.userId()),
                new NotificationReferenceId(event.referenceId()), event.type(), event.occurredAt(), event.factId()))
                .toOptional().orElseThrow();
    }
}

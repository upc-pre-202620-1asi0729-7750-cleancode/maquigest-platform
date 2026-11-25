package com.maquigest.platform.profiles.application.internal.eventhandlers;

import com.maquigest.platform.profiles.application.commandservices.NotificationCommandService;
import com.maquigest.platform.profiles.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.profiles.domain.model.events.*;
import com.maquigest.platform.profiles.domain.model.valueobjects.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;
import java.util.UUID;

@Component
public class ProfileNotificationEventHandler {
    private final NotificationCommandService commands;
    public ProfileNotificationEventHandler(NotificationCommandService commands) { this.commands = commands; }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CompanyProfileCreatedEvent event) {
        create(event.userId(), event.profileId(), NotificationType.PROFILE_CREATED, event.factId(), event.occurredAt());
    }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(CompanyProfileUpdatedEvent event) {
        create(event.userId(), event.profileId(), NotificationType.PROFILE_UPDATED, event.factId(), event.occurredAt());
    }
    private void create(Long userId, Long referenceId, NotificationType type, UUID factId, Instant occurredAt) {
        commands.handle(new CreateNotificationCommand(new NotificationRecipientId(userId),
                new NotificationReferenceId(referenceId), type, occurredAt, factId)).toOptional().orElseThrow();
    }
}

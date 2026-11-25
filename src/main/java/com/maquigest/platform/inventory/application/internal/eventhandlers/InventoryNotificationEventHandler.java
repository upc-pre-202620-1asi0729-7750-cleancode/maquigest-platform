package com.maquigest.platform.inventory.application.internal.eventhandlers;

import com.maquigest.platform.inventory.application.commandservices.NotificationCommandService;
import com.maquigest.platform.inventory.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.inventory.domain.model.events.*;
import com.maquigest.platform.inventory.domain.model.valueobjects.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;
import java.util.UUID;

@Component
public class InventoryNotificationEventHandler {
    private final NotificationCommandService commands;
    public InventoryNotificationEventHandler(NotificationCommandService commands) { this.commands = commands; }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(EquipmentCreatedEvent event) {
        create(event.userId(), event.equipmentId(), NotificationType.EQUIPMENT_CREATED, event.factId(), event.occurredAt());
    }
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void on(EquipmentStatusChangedEvent event) {
        create(event.userId(), event.equipmentId(), NotificationType.EQUIPMENT_STATUS_CHANGED, event.factId(), event.occurredAt());
    }
    private void create(Long userId, Long referenceId, NotificationType type, UUID factId, Instant occurredAt) {
        commands.handle(new CreateNotificationCommand(new NotificationRecipientId(userId),
                new NotificationReferenceId(referenceId), type, occurredAt, factId)).toOptional().orElseThrow();
    }
}

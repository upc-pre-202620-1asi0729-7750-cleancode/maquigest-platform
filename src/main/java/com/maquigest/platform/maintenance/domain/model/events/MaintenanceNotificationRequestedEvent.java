package com.maquigest.platform.maintenance.domain.model.events;

import com.maquigest.platform.maintenance.domain.model.valueobjects.NotificationType;
import java.time.Instant;
import java.util.UUID;

/** Local fact with an assigned aggregate identity and recipient. */
public record MaintenanceNotificationRequestedEvent(Long referenceId, Long userId, NotificationType type, UUID factId, Instant occurredAt) {
    public MaintenanceNotificationRequestedEvent(Long referenceId, Long userId, NotificationType type) {
        this(referenceId, userId, type, UUID.randomUUID(), Instant.now());
    }
}

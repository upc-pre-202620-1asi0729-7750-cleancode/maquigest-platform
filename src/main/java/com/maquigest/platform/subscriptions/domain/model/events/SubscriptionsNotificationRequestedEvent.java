package com.maquigest.platform.subscriptions.domain.model.events;

import com.maquigest.platform.subscriptions.domain.model.valueobjects.NotificationType;
import java.time.Instant;
import java.util.UUID;

/** Local fact with an assigned aggregate identity and recipient. */
public record SubscriptionsNotificationRequestedEvent(Long referenceId, Long userId, NotificationType type, UUID factId, Instant occurredAt) {
    public SubscriptionsNotificationRequestedEvent(Long referenceId, Long userId, NotificationType type) {
        this(referenceId, userId, type, UUID.randomUUID(), Instant.now());
    }
}

package com.maquigest.platform.iam.domain.model.events;

import com.maquigest.platform.iam.domain.model.valueobjects.NotificationType;
import java.time.Instant;
import java.util.UUID;

/** Local fact with an assigned aggregate identity and recipient. */
public record IamNotificationRequestedEvent(Long referenceId, Long userId, NotificationType type, UUID factId, Instant occurredAt) {
    public IamNotificationRequestedEvent(Long referenceId, Long userId, NotificationType type) {
        this(referenceId, userId, type, UUID.randomUUID(), Instant.now());
    }
}

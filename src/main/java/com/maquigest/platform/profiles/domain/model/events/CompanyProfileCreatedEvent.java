package com.maquigest.platform.profiles.domain.model.events;

import java.time.Instant;
import java.util.UUID;

public record CompanyProfileCreatedEvent(Long profileId, Long userId, UUID factId, Instant occurredAt) {
    public CompanyProfileCreatedEvent(Long profileId, Long userId) {
        this(profileId, userId, UUID.randomUUID(), Instant.now());
    }
}

package com.maquigest.platform.inventory.domain.model.events;

import java.time.Instant;
import java.util.UUID;

public record EquipmentCreatedEvent(Long equipmentId, Long userId, UUID factId, Instant occurredAt) {
    public EquipmentCreatedEvent(Long equipmentId, Long userId) {
        this(equipmentId, userId, UUID.randomUUID(), Instant.now());
    }
}

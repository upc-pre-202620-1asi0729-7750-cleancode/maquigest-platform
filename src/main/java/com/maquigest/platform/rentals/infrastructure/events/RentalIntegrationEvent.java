package com.maquigest.platform.rentals.infrastructure.events;
import java.time.Instant;
public record RentalIntegrationEvent(String eventType,Long aggregateId,Long equipmentId,Instant occurredAt) { }

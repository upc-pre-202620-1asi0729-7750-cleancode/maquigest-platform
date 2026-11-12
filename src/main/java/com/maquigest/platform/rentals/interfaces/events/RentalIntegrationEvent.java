package com.maquigest.platform.rentals.interfaces.events;
import java.time.Instant;
public record RentalIntegrationEvent(String eventType,Long aggregateId,Long equipmentId,Instant occurredAt) { }

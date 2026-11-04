package com.maquigest.platform.maintenance.infrastructure.events;
import java.time.Instant;
public record MaintenanceIntegrationEvent(String type,Long entityId,Long equipmentId,Instant occurredAt) { }

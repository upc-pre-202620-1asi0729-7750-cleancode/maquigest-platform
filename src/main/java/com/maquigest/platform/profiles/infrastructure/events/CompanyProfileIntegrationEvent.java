package com.maquigest.platform.profiles.infrastructure.events;
import java.time.Instant;
/** Local in-process integration notification, not durable broker delivery. */
public record CompanyProfileIntegrationEvent(String eventName,Long profileId,Long userId,Instant occurredAt) { }

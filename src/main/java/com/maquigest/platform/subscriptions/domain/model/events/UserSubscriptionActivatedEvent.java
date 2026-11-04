package com.maquigest.platform.subscriptions.domain.model.events;
import java.time.Instant;
/** Domain event for future local handlers / integration-event translation after commit. */
public record UserSubscriptionActivatedEvent(Long userId, Long planId, Instant effectiveFrom, Instant effectiveUntil) { }

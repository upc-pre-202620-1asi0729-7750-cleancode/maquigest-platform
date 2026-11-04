package com.maquigest.platform.subscriptions.domain.model.events;
import java.time.Instant;
public record UserSubscriptionCancelledEvent(Long userId, Long subscriptionId, Instant cancelledAt) { }

package com.maquigest.platform.subscriptions.interfaces.rest.resources;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import java.time.Instant;
public record UserSubscriptionResource(Long id, Long userId, Long planId,
    Instant startDate, Instant endDate, SubscriptionStatus status, boolean autoRenew) { }

package com.maquigest.platform.subscriptions.domain.model.commands;
public record CancelUserSubscriptionCommand(Long subscriptionId, Long actorUserId) {
    public CancelUserSubscriptionCommand {
        if (subscriptionId == null || subscriptionId <= 0) throw new IllegalArgumentException("subscriptionId must be positive");
        if (actorUserId == null || actorUserId <= 0) throw new IllegalArgumentException("actorUserId must be positive");
    }
}

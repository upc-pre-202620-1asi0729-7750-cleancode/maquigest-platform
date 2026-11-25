package com.maquigest.platform.subscriptions.domain.model.commands;
public record CreateSubscriptionCheckoutCommand(Long userId,Long planId) {
    public CreateSubscriptionCheckoutCommand {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
        if (planId == null || planId <= 0) throw new IllegalArgumentException("planId must be positive");
    }
}

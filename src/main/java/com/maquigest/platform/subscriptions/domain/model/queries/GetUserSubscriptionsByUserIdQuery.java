package com.maquigest.platform.subscriptions.domain.model.queries;

public record GetUserSubscriptionsByUserIdQuery(Long userId) {
    public GetUserSubscriptionsByUserIdQuery {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
    }
}

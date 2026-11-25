package com.maquigest.platform.subscriptions.domain.model.queries;

public record GetUserSubscriptionByIdAndUserIdQuery(Long id, Long userId) {
    public GetUserSubscriptionByIdAndUserIdQuery {
        if (id == null || id <= 0) throw new IllegalArgumentException("id must be positive");
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
    }
}

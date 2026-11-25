package com.maquigest.platform.subscriptions.domain.model.valueobjects;

/** External identity as understood by the subscriptions bounded context. */
public record UserId(Long value) {
    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}

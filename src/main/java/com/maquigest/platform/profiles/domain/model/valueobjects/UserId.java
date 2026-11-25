package com.maquigest.platform.profiles.domain.model.valueobjects;

/** External identity as understood by the profiles bounded context. */
public record UserId(Long value) {
    public UserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }
}

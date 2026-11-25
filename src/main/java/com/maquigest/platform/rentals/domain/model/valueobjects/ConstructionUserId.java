package com.maquigest.platform.rentals.domain.model.valueobjects;

/** External identity as understood by the rentals bounded context. */
public record ConstructionUserId(Long value) {
    public ConstructionUserId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("constructionUserId must be positive");
        }
    }
}

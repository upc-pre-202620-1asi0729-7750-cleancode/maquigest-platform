package com.maquigest.platform.iam.domain.model.valueobjects;

import java.util.Locale;

public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("email is required");
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("email must be valid");
        }
    }
}

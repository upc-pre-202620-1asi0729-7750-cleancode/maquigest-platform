package com.maquigest.platform.profiles.domain.model.valueobjects;

public record PhoneNumber(String value) {
    public PhoneNumber {
        value = value == null ? "" : value.trim();
        if (value.length() > 40) throw new IllegalArgumentException("phoneNumber exceeds 40 characters");
    }
}

package com.maquigest.platform.subscriptions.domain.model.valueobjects;

public record BillingCycle(String value) {
    public BillingCycle {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("billingCycle is required");
        value = value.trim();
    }
}

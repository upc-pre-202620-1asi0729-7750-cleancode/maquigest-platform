package com.maquigest.platform.subscriptions.domain.model.valueobjects;
import java.math.BigDecimal;
import java.util.Objects;
/** Immutable monetary value; subscriptions use PEN in the current plan catalog. */
public record SubscriptionPrice(BigDecimal amount, String currency) {
    public SubscriptionPrice {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() < 0) throw new IllegalArgumentException("amount must be nonnegative");
        if (currency == null || !currency.matches("[A-Z]{3}"))
            throw new IllegalArgumentException("currency must be an ISO 4217 code");
    }
}

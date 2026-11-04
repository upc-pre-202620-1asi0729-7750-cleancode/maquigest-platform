package com.maquigest.platform.inventory.domain.model.valueobjects;
import java.math.BigDecimal;
/** Immutable monetary rates, independent of JPA. */
public record RentalRate(BigDecimal dailyRate, BigDecimal weeklyRate) {
    public RentalRate {
        validate(dailyRate, "dailyRate");
        validate(weeklyRate, "weeklyRate");
    }
    private static void validate(BigDecimal rate, String field) {
        if (rate == null || rate.signum() <= 0 || rate.scale() > 2 || rate.precision() > 12) {
            throw new IllegalArgumentException(field + " must be positive with at most two decimals");
        }
    }
}

package com.maquigest.platform.subscriptions.domain.model.valueobjects;
import java.time.Instant;
import java.util.Objects;
/** Date range value object: start and end instants are inclusive. */
public record SubscriptionPeriod(Instant startDate, Instant endDate) {
    public SubscriptionPeriod {
        Objects.requireNonNull(startDate,"startDate");
        Objects.requireNonNull(endDate,"endDate");
        if (!endDate.isAfter(startDate)) throw new IllegalArgumentException("endDate must be after startDate");
    }
    public boolean contains(Instant instant) {
        return !instant.isBefore(startDate) && !instant.isAfter(endDate);
    }
}

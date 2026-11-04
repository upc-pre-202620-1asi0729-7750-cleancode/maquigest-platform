package com.maquigest.platform.subscriptions.interfaces.rest.resources;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
/** Accepts the full existing WebApp resource; the client-sent id/status never grant privileges. */
public record CreateUserSubscriptionResource(
    Long id,
    @NotNull @Positive Long userId,
    @NotNull @Positive Long planId,
    @NotNull Instant startDate,
    @NotNull Instant endDate,
    @NotNull SubscriptionStatus status,
    @NotNull Boolean autoRenew) { }

package com.maquigest.platform.subscriptions.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record CreateSubscriptionCheckoutResource(@NotNull @Positive Long planId) {}

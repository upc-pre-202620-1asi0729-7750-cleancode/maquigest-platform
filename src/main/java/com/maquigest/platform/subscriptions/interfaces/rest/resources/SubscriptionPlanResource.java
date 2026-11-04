package com.maquigest.platform.subscriptions.interfaces.rest.resources;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import java.math.BigDecimal;
public record SubscriptionPlanResource(Long id, String name, String description,
    BigDecimal priceAmount, String priceCurrency, String billingCycle, PlanStatus status) { }

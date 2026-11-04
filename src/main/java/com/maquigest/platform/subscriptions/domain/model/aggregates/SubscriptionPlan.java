package com.maquigest.platform.subscriptions.domain.model.aggregates;

import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import java.math.BigDecimal;
import java.util.Objects;

/** Catalog aggregate. Plan identity is local to Subscriptions, not IAM/Inventory. */
public final class SubscriptionPlan extends AbstractDomainAggregateRoot<SubscriptionPlan> {
    private final Long id;
    private final String name;
    private final String description;
    private final com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionPrice price;
    private final String billingCycle;
    private final PlanStatus status;

    public SubscriptionPlan(Long id, String name, String description, BigDecimal priceAmount,
                            String priceCurrency, String billingCycle, PlanStatus status) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id = id;
        this.name = required(name, "name");
        this.description = required(description, "description");
        this.price = new com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionPrice(
            priceAmount, priceCurrency);
        this.billingCycle = required(billingCycle, "billingCycle");
        this.status = Objects.requireNonNull(status, "status");
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    public boolean isActive() { return status == PlanStatus.ACTIVE; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPriceAmount() { return price.amount(); }
    public String getPriceCurrency() { return price.currency(); }
    public String getBillingCycle() { return billingCycle; }
    public PlanStatus getStatus() { return status; }
}

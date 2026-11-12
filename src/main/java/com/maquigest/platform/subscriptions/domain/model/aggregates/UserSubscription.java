package com.maquigest.platform.subscriptions.domain.model.aggregates;

import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import java.time.Instant;
import java.util.Objects;

/** Subscription aggregate. Only subscription IDs are stored; IAM/Inventory entities are not embedded. */
public final class UserSubscription extends AbstractDomainAggregateRoot<UserSubscription> {
    private final Long id;
    private final Long userId;
    private Long planId;
    private final com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionPeriod period;
    private SubscriptionStatus status;
    private final boolean autoRenew;

    public UserSubscription(Long id, Long userId, Long planId, Instant startDate,
                            Instant endDate, SubscriptionStatus status, boolean autoRenew) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
        if (planId == null || planId <= 0) throw new IllegalArgumentException("planId must be positive");
        this.id = id;
        this.userId = userId;
        this.planId = planId;
        this.period = new com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionPeriod(
            startDate, endDate);
        this.status = Objects.requireNonNull(status, "status");
        this.autoRenew = autoRenew;
        if (id == null) registerDomainEvent(
            new com.maquigest.platform.subscriptions.domain.model.events.UserSubscriptionActivatedEvent(
                userId, planId, startDate, endDate));
    }
    public boolean isActiveAt(Instant at) {
        return status == SubscriptionStatus.ACTIVE && period.contains(at);
    }
    public boolean occupiesActivePeriodAt(Instant at) {
        return status == SubscriptionStatus.ACTIVE && period.endDate().isAfter(at);
    }
    /** Change entitlement immediately, without altering the paid period or subscription identity. */
    public void changePlan(Long newPlanId) {
        if (!isActiveAt(Instant.now()))
            throw new IllegalStateException("Only a currently ACTIVE subscription can change plan");
        if (newPlanId == null || newPlanId <= 0)
            throw new IllegalArgumentException("The new plan id must be positive");
        if (planId.equals(newPlanId))
            throw new IllegalStateException("The selected plan is already active");
        planId = newPlanId;
    }

    public void cancel() {
        if (status != SubscriptionStatus.ACTIVE)
            throw new IllegalStateException("Only an ACTIVE subscription can be cancelled");
        status = SubscriptionStatus.CANCELLED;
        registerDomainEvent(new com.maquigest.platform.subscriptions.domain.model.events.UserSubscriptionCancelledEvent(
            userId, id, Instant.now()));
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getPlanId() { return planId; }
    public Instant getStartDate() { return period.startDate(); }
    public Instant getEndDate() { return period.endDate(); }
    public SubscriptionStatus getStatus() { return status; }
    public boolean isAutoRenew() { return autoRenew; }
}

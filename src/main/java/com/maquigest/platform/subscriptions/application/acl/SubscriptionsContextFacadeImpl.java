package com.maquigest.platform.subscriptions.application.acl;

import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionPlanRepository;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import com.maquigest.platform.subscriptions.interfaces.acl.SubscriptionsContextFacade;
import java.time.Instant;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Subscriptions owns all entitlement decisions, preserving original plan semantics. */
@Service
@Transactional(readOnly = true)
public class SubscriptionsContextFacadeImpl implements SubscriptionsContextFacade {
    private final UserSubscriptionRepository subscriptions;
    private final SubscriptionPlanRepository plans;
    public SubscriptionsContextFacadeImpl(UserSubscriptionRepository subscriptions, SubscriptionPlanRepository plans) {
        this.subscriptions = subscriptions;
        this.plans = plans;
    }
    private boolean eligible(Long userId, Predicate<SubscriptionPlan> condition) {
        if (userId == null || userId <= 0) return false;
        var now = Instant.now();
        return subscriptions.findAll().stream().anyMatch(s ->
            s.getUserId().equals(userId) && s.isActiveAt(now)
                && plans.findById(s.getPlanId())
                    .map(p -> p.isActive() && condition.test(p)).orElse(false));
    }
    @Override public boolean hasActiveSubscription(Long userId) {
        return eligible(userId, p -> true);
    }
    @Override public boolean canManageRentals(Long userId) {
        return eligible(userId, p -> "Professional".equalsIgnoreCase(p.getName())
            || "Growth".equalsIgnoreCase(p.getName()));
    }
    @Override public boolean hasActiveGrowth(Long userId) {
        return eligible(userId, p -> "Growth".equalsIgnoreCase(p.getName()));
    }
}

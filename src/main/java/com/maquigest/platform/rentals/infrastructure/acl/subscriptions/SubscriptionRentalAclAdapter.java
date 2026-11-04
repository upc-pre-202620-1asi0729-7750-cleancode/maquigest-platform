package com.maquigest.platform.rentals.infrastructure.acl.subscriptions;
import com.maquigest.platform.rentals.application.ports.RentalSubscriptionPort;
import com.maquigest.platform.subscriptions.domain.repositories.*;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component
public class SubscriptionRentalAclAdapter implements RentalSubscriptionPort {
    private final UserSubscriptionRepository userSubscriptions;
    private final SubscriptionPlanRepository plans;
    public SubscriptionRentalAclAdapter(UserSubscriptionRepository userSubscriptions,SubscriptionPlanRepository plans) {
        this.userSubscriptions=userSubscriptions;this.plans=plans;
    }
    @Override public boolean hasActiveSubscription(Long owner) {
        if(owner==null || owner<=0) return false;
        var now=Instant.now();
        return userSubscriptions.findAll().stream().anyMatch(s->s.getUserId().equals(owner) && s.isActiveAt(now)
          && plans.findById(s.getPlanId()).map(p->p.isActive()).orElse(false));
    }
    @Override public boolean canManageRentals(Long owner) {
        if(owner==null || owner<=0) return false;
        var now=Instant.now();
        return userSubscriptions.findAll().stream().anyMatch(s->s.getUserId().equals(owner) && s.isActiveAt(now)
          && plans.findById(s.getPlanId()).map(p->p.isActive() &&
            ("Professional".equalsIgnoreCase(p.getName())||"Growth".equalsIgnoreCase(p.getName()))).orElse(false));
    }
}

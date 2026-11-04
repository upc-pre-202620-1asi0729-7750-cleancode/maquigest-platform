package com.maquigest.platform.maintenance.infrastructure.acl.subscriptions;
import com.maquigest.platform.maintenance.application.ports.MaintenanceSubscriptionPort;
import com.maquigest.platform.subscriptions.domain.repositories.*;
import java.time.Instant;
import org.springframework.stereotype.Component;
@Component
public class SubscriptionMaintenanceAclAdapter implements MaintenanceSubscriptionPort {
    private final UserSubscriptionRepository subscriptions;
    private final SubscriptionPlanRepository plans;
    public SubscriptionMaintenanceAclAdapter(UserSubscriptionRepository subscriptions,SubscriptionPlanRepository plans){
        this.subscriptions=subscriptions;this.plans=plans;
    }
    @Override public boolean hasActiveGrowth(Long userId){
        if(userId==null||userId<=0)return false;
        var now=Instant.now();
        return subscriptions.findAll().stream().anyMatch(s->s.getUserId().equals(userId)&&s.isActiveAt(now)
            &&plans.findById(s.getPlanId()).map(p->p.isActive()&&"Growth".equalsIgnoreCase(p.getName())).orElse(false));
    }
}

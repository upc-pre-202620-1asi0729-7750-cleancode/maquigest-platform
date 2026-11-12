package com.maquigest.platform.maintenance.infrastructure.acl.subscriptions;

import com.maquigest.platform.maintenance.application.ports.MaintenanceSubscriptionPort;
import com.maquigest.platform.subscriptions.interfaces.acl.SubscriptionsContextFacade;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionMaintenanceAclAdapter implements MaintenanceSubscriptionPort {
    private final SubscriptionsContextFacade subscriptions;
    public SubscriptionMaintenanceAclAdapter(SubscriptionsContextFacade subscriptions) {
        this.subscriptions = subscriptions;
    }
    @Override public boolean hasActiveGrowth(Long userId) {
        return subscriptions.hasActiveGrowth(userId);
    }
}

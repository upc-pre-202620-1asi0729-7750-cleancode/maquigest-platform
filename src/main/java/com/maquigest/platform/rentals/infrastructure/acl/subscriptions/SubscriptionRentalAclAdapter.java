package com.maquigest.platform.rentals.infrastructure.acl.subscriptions;

import com.maquigest.platform.rentals.application.ports.RentalSubscriptionPort;
import com.maquigest.platform.subscriptions.interfaces.acl.SubscriptionsContextFacade;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionRentalAclAdapter implements RentalSubscriptionPort {
    private final SubscriptionsContextFacade subscriptions;
    public SubscriptionRentalAclAdapter(SubscriptionsContextFacade subscriptions) {
        this.subscriptions = subscriptions;
    }
    @Override public boolean hasActiveSubscription(Long userId) {
        return subscriptions.hasActiveSubscription(userId);
    }
    @Override public boolean canManageRentals(Long userId) {
        return subscriptions.canManageRentals(userId);
    }
}

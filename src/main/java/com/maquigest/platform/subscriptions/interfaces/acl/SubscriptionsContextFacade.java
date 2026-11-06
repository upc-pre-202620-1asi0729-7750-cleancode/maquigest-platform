package com.maquigest.platform.subscriptions.interfaces.acl;
/** Public Subscriptions contract for other bounded contexts. */
public interface SubscriptionsContextFacade {
    boolean hasActiveSubscription(Long userId);
    boolean canManageRentals(Long userId);
    boolean hasActiveGrowth(Long userId);
}

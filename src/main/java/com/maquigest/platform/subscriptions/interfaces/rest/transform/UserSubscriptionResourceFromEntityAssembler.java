package com.maquigest.platform.subscriptions.interfaces.rest.transform;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.UserSubscriptionResource;
public final class UserSubscriptionResourceFromEntityAssembler {
    private UserSubscriptionResourceFromEntityAssembler() { }
    public static UserSubscriptionResource toResourceFromEntity(UserSubscription subscription) {
        return new UserSubscriptionResource(subscription.getId(), subscription.getUserId(),
            subscription.getPlanId(),subscription.getStartDate(),subscription.getEndDate(),
            subscription.getStatus(),subscription.isAutoRenew());
    }
}

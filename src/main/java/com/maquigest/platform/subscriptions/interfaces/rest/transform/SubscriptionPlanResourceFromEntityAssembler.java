package com.maquigest.platform.subscriptions.interfaces.rest.transform;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.SubscriptionPlanResource;
public final class SubscriptionPlanResourceFromEntityAssembler {
    private SubscriptionPlanResourceFromEntityAssembler() { }
    public static SubscriptionPlanResource toResourceFromEntity(SubscriptionPlan plan) {
        return new SubscriptionPlanResource(plan.getId(),plan.getName(),plan.getDescription(),
            plan.getPriceAmount(),plan.getPriceCurrency(),plan.getBillingCycle(),plan.getStatus());
    }
}

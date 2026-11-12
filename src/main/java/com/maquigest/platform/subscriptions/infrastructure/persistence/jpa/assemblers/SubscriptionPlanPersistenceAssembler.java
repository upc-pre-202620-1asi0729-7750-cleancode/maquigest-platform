package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
public final class SubscriptionPlanPersistenceAssembler {
    private SubscriptionPlanPersistenceAssembler() { }
    public static SubscriptionPlan toDomain(SubscriptionPlanPersistenceEntity entity) {
        return new SubscriptionPlan(entity.getId(),entity.getName(),entity.getDescription(),entity.getPriceAmount(),
            entity.getPriceCurrency(),entity.getBillingCycle(),entity.getStatus());
    }
}

package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionCheckoutPersistenceEntity;
public final class SubscriptionCheckoutPersistenceAssembler {
    private SubscriptionCheckoutPersistenceAssembler() {}
    public static SubscriptionCheckout toDomain(SubscriptionCheckoutPersistenceEntity e) {
        return SubscriptionCheckout.reconstitute(e.getId(),e.getReference(),e.getUserId(),e.getPlanId(),e.getAmountMinor(),e.getExpiresAt(),e.getSessionId(),e.getStatus(),e.getSubscriptionId());
    }
    public static void copyToPersistence(SubscriptionCheckout c,SubscriptionCheckoutPersistenceEntity e) {
        e.setReference(c.getReference());e.setUserId(c.getUserId());e.setPlanId(c.getPlanId());e.setAmountMinor(c.getAmountMinor());
        e.setExpiresAt(c.getExpiresAt());e.setSessionId(c.getSessionId());e.setStatus(c.getStatus());e.setSubscriptionId(c.getSubscriptionId());
    }
}

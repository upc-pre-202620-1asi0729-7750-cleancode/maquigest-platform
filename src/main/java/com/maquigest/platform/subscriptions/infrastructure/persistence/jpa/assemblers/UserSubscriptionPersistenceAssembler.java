package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.UserSubscriptionPersistenceEntity;
public final class UserSubscriptionPersistenceAssembler {
    private UserSubscriptionPersistenceAssembler() { }
    public static UserSubscription toDomain(UserSubscriptionPersistenceEntity entity) {
        return new UserSubscription(entity.getId(),entity.getUserId(),entity.getPlanId(),
            entity.getStartDate(),entity.getEndDate(),entity.getStatus(),entity.isAutoRenew());
    }
    public static UserSubscriptionPersistenceEntity toPersistence(UserSubscription domain) {
        var entity = new UserSubscriptionPersistenceEntity();
        entity.setId(domain.getId()); entity.setUserId(domain.getUserId()); entity.setPlanId(domain.getPlanId());
        entity.setStartDate(domain.getStartDate()); entity.setEndDate(domain.getEndDate());
        entity.setStatus(domain.getStatus()); entity.setAutoRenew(domain.isAutoRenew());
        return entity;
    }
}

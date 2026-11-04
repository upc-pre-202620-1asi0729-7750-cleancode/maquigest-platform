package com.maquigest.platform.subscriptions.domain.repositories;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import java.util.List;
import java.util.Optional;
public interface SubscriptionPlanRepository {
    List<SubscriptionPlan> findAll();
    Optional<SubscriptionPlan> findById(Long id);
}

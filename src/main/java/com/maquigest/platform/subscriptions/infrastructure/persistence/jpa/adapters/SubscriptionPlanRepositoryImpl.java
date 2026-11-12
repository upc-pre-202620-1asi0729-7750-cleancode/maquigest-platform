package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionPlanRepository;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionPlanPersistenceRepository;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers.SubscriptionPlanPersistenceAssembler;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public class SubscriptionPlanRepositoryImpl implements SubscriptionPlanRepository {
    private final SubscriptionPlanPersistenceRepository jpa;
    public SubscriptionPlanRepositoryImpl(SubscriptionPlanPersistenceRepository jpa) { this.jpa=jpa; }
    @Override public List<SubscriptionPlan> findAll() { return jpa.findAllByOrderByIdAsc().stream().map(SubscriptionPlanPersistenceAssembler::toDomain).toList(); }
    @Override public Optional<SubscriptionPlan> findById(Long id) { return jpa.findById(id).map(SubscriptionPlanPersistenceAssembler::toDomain); }
}

package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SubscriptionPlanPersistenceRepository extends JpaRepository<SubscriptionPlanPersistenceEntity,Long> {
    Optional<SubscriptionPlanPersistenceEntity> findByNameIgnoreCase(String name);
    List<SubscriptionPlanPersistenceEntity> findAllByOrderByIdAsc();
}

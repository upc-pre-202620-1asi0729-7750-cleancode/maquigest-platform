package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.UserSubscriptionPersistenceEntity;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
public interface UserSubscriptionPersistenceRepository extends JpaRepository<UserSubscriptionPersistenceEntity,Long> {
    List<UserSubscriptionPersistenceEntity> findAllByOrderByIdAsc();
    boolean existsByUserIdAndStatusAndEndDateAfter(Long userId, SubscriptionStatus status, Instant now);
}

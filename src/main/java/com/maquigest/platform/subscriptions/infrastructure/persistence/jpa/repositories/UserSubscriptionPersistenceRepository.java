package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.UserSubscriptionPersistenceEntity;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
public interface UserSubscriptionPersistenceRepository extends JpaRepository<UserSubscriptionPersistenceEntity,Long> {
    List<UserSubscriptionPersistenceEntity> findAllByOrderByIdAsc();
    List<UserSubscriptionPersistenceEntity> findAllByUserIdOrderByIdAsc(Long userId);
    Optional<UserSubscriptionPersistenceEntity> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndStatusAndEndDateAfter(Long userId, SubscriptionStatus status, Instant now);
}

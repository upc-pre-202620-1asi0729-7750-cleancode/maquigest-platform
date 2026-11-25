package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionCheckoutPersistenceEntity;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface SubscriptionCheckoutPersistenceRepository extends JpaRepository<SubscriptionCheckoutPersistenceEntity,Long> {
    Optional<SubscriptionCheckoutPersistenceEntity> findByReference(String reference);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SubscriptionCheckoutPersistenceEntity> findFirstByUserIdAndStatusOrderByIdDesc(Long userId,CheckoutStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from SubscriptionCheckoutPersistenceEntity c where c.reference=:reference")
    Optional<SubscriptionCheckoutPersistenceEntity> findByReferenceForUpdate(@Param("reference") String reference);
}

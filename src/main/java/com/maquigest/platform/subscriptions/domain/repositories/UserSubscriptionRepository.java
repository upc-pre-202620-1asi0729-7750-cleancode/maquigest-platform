package com.maquigest.platform.subscriptions.domain.repositories;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import java.util.List;
import java.util.Optional;
public interface UserSubscriptionRepository {
    List<UserSubscription> findAll();
    Optional<UserSubscription> findById(Long id);
    List<UserSubscription> findAllByUserId(Long userId);
    Optional<UserSubscription> findByIdAndUserId(Long id, Long userId);
    boolean hasUnexpiredActiveSubscription(Long userId, java.time.Instant at);
    UserSubscription save(UserSubscription subscription);
}

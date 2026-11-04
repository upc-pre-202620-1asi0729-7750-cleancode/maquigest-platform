package com.maquigest.platform.subscriptions.domain.repositories;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import java.util.List;
import java.util.Optional;
public interface UserSubscriptionRepository {
    List<UserSubscription> findAll();
    Optional<UserSubscription> findById(Long id);
    boolean hasUnexpiredActiveSubscription(Long userId, java.time.Instant at);
    UserSubscription save(UserSubscription subscription);
}

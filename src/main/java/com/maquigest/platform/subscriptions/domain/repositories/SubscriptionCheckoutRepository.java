package com.maquigest.platform.subscriptions.domain.repositories;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import java.util.Optional;
public interface SubscriptionCheckoutRepository {
    Optional<SubscriptionCheckout> findPendingByUserId(Long userId);
    Optional<SubscriptionCheckout> findByReference(String reference);
    Optional<SubscriptionCheckout> findByReferenceForUpdate(String reference);
    SubscriptionCheckout save(SubscriptionCheckout checkout);
}

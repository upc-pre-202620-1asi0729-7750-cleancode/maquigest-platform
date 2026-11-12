package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers.UserSubscriptionPersistenceAssembler;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories.UserSubscriptionPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
@Repository
public class UserSubscriptionRepositoryImpl implements UserSubscriptionRepository {
    private final UserSubscriptionPersistenceRepository jpa;
    private final com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher publisher;
    public UserSubscriptionRepositoryImpl(UserSubscriptionPersistenceRepository jpa,
        com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher publisher) {
        this.jpa=jpa; this.publisher=publisher;
    }
    @Override public List<UserSubscription> findAll() {
        return jpa.findAllByOrderByIdAsc().stream().map(UserSubscriptionPersistenceAssembler::toDomain).toList();
    }
    @Override public Optional<UserSubscription> findById(Long id) {
        return jpa.findById(id).map(UserSubscriptionPersistenceAssembler::toDomain);
    }
    @Override public boolean hasUnexpiredActiveSubscription(Long userId, Instant now) {
        return jpa.existsByUserIdAndStatusAndEndDateAfter(userId,SubscriptionStatus.ACTIVE,now);
    }
    @Override public UserSubscription save(UserSubscription domain) {
        // A new entity has a null identity; save/update keep the previous version lock state in JPA.
        if (domain.getId() != null) {
            var existing = jpa.findById(domain.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription no longer exists"));
            existing.setStatus(domain.getStatus());
            // Persist new entitlement on plan changes; the previous implementation saved status only.
            existing.setPlanId(domain.getPlanId());
            var saved = jpa.saveAndFlush(existing);
            publisher.publishAndClearEvents(domain);
            return UserSubscriptionPersistenceAssembler.toDomain(saved);
        }
        var saved = jpa.saveAndFlush(UserSubscriptionPersistenceAssembler.toPersistence(domain));
        publisher.publishAndClearEvents(domain);
        return UserSubscriptionPersistenceAssembler.toDomain(saved);
    }
}

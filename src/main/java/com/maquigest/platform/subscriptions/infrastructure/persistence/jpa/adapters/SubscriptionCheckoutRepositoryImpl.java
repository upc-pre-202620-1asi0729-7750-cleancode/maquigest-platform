package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionCheckoutRepository;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionCheckoutPersistenceEntity;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionCheckoutPersistenceRepository;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers.SubscriptionCheckoutPersistenceAssembler;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public class SubscriptionCheckoutRepositoryImpl implements SubscriptionCheckoutRepository {
    private final SubscriptionCheckoutPersistenceRepository persistence;
    private final DomainEventPublisher domainEventPublisher;
    public SubscriptionCheckoutRepositoryImpl(SubscriptionCheckoutPersistenceRepository persistence,
            DomainEventPublisher domainEventPublisher) {
        this.persistence = persistence;
        this.domainEventPublisher = domainEventPublisher;
    }
    public Optional<SubscriptionCheckout> findPendingByUserId(Long id) {return persistence.findFirstByUserIdAndStatusOrderByIdDesc(id,CheckoutStatus.PENDING).map(SubscriptionCheckoutPersistenceAssembler::toDomain);}
    public Optional<SubscriptionCheckout> findByReference(String ref) {return persistence.findByReference(ref).map(SubscriptionCheckoutPersistenceAssembler::toDomain);}
    public Optional<SubscriptionCheckout> findByReferenceForUpdate(String ref) {return persistence.findByReferenceForUpdate(ref).map(SubscriptionCheckoutPersistenceAssembler::toDomain);}
    public SubscriptionCheckout save(SubscriptionCheckout c) {
        var e=c.getId()==null?new SubscriptionCheckoutPersistenceEntity():persistence.findById(c.getId()).orElseThrow();
        SubscriptionCheckoutPersistenceAssembler.copyToPersistence(c,e);
        var saved = SubscriptionCheckoutPersistenceAssembler.toDomain(persistence.saveAndFlush(e));
        domainEventPublisher.publishAndClearEvents(c);
        return saved;
    }
}

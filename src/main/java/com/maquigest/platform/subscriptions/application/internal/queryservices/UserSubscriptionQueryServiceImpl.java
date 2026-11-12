package com.maquigest.platform.subscriptions.application.internal.queryservices;

import com.maquigest.platform.subscriptions.application.queryservices.UserSubscriptionQueryService;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllUserSubscriptionsQuery;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class UserSubscriptionQueryServiceImpl implements UserSubscriptionQueryService {

    private final UserSubscriptionRepository repository;

    public UserSubscriptionQueryServiceImpl(UserSubscriptionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSubscription> handle(GetAllUserSubscriptionsQuery query) {
        Instant now = Instant.now();
        return repository.findAll().stream()
            .map(subscription -> projectEffectiveStatus(subscription, now))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSubscription> findById(Long id) {
        Instant now = Instant.now();
        return repository.findById(id)
            .map(subscription -> projectEffectiveStatus(subscription, now));
    }

    /**
     * Proyecta una suscripcion ya vencida como EXPIRED para las consultas.
     * No modifica el agregado original ni actualiza su estado persistido.
     */
    private static UserSubscription projectEffectiveStatus(UserSubscription subscription, Instant now) {
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE
            || !subscription.getEndDate().isBefore(now)) {
            return subscription;
        }

        // El ID no es nulo: reconstruir el agregado no registra un evento de alta.
        return new UserSubscription(
            subscription.getId(),
            subscription.getUserId(),
            subscription.getPlanId(),
            subscription.getStartDate(),
            subscription.getEndDate(),
            SubscriptionStatus.EXPIRED,
            subscription.isAutoRenew()
        );
    }
}

package com.maquigest.platform.subscriptions.application.queryservices;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllUserSubscriptionsQuery;
import java.util.List;
import java.util.Optional;
public interface UserSubscriptionQueryService {
    List<UserSubscription> handle(GetAllUserSubscriptionsQuery query);
    Optional<UserSubscription> findById(Long id);
}

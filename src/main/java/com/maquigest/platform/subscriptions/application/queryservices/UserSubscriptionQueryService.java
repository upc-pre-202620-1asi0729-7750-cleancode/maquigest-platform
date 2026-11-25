package com.maquigest.platform.subscriptions.application.queryservices;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllUserSubscriptionsQuery;
import com.maquigest.platform.subscriptions.domain.model.queries.GetActiveProviderUserIdsQuery;
import com.maquigest.platform.subscriptions.domain.model.queries.GetUserSubscriptionsByUserIdQuery;
import com.maquigest.platform.subscriptions.domain.model.queries.GetUserSubscriptionByIdAndUserIdQuery;
import java.util.List;
import java.util.Optional;
public interface UserSubscriptionQueryService {
    List<UserSubscription> handle(GetAllUserSubscriptionsQuery query);
    List<Long> handle(GetActiveProviderUserIdsQuery query);
    Optional<UserSubscription> findById(Long id);
    List<UserSubscription> handle(GetUserSubscriptionsByUserIdQuery query);
    Optional<UserSubscription> handle(GetUserSubscriptionByIdAndUserIdQuery query);
}

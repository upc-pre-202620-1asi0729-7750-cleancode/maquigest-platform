package com.maquigest.platform.subscriptions.application.queryservices;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllSubscriptionPlansQuery;
import java.util.List;
public interface SubscriptionPlanQueryService { List<SubscriptionPlan> handle(GetAllSubscriptionPlansQuery query); }

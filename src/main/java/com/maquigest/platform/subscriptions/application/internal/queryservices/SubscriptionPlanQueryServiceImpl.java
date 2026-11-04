package com.maquigest.platform.subscriptions.application.internal.queryservices;
import com.maquigest.platform.subscriptions.application.queryservices.SubscriptionPlanQueryService;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllSubscriptionPlansQuery;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class SubscriptionPlanQueryServiceImpl implements SubscriptionPlanQueryService {
    private final SubscriptionPlanRepository repository;
    public SubscriptionPlanQueryServiceImpl(SubscriptionPlanRepository repository) { this.repository = repository; }
    @Override @Transactional(readOnly = true)
    public List<SubscriptionPlan> handle(GetAllSubscriptionPlansQuery query) { return repository.findAll(); }
}

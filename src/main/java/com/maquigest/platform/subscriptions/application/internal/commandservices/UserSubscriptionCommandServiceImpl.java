package com.maquigest.platform.subscriptions.application.internal.commandservices;
import com.maquigest.platform.shared.application.result.*;
import com.maquigest.platform.subscriptions.application.commandservices.UserSubscriptionCommandService;
import com.maquigest.platform.subscriptions.application.ports.IamSubscriberPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class UserSubscriptionCommandServiceImpl implements UserSubscriptionCommandService {
    private final UserSubscriptionRepository subscriptions;
    private final SubscriptionPlanRepository plans;
    private final IamSubscriberPort iam;
    public UserSubscriptionCommandServiceImpl(UserSubscriptionRepository subscriptions,
            SubscriptionPlanRepository plans, IamSubscriberPort iam) {
        this.subscriptions = subscriptions; this.plans = plans; this.iam = iam;
    }
    @Override @Transactional
    public Result<UserSubscription, ApplicationError> handle(CreateUserSubscriptionCommand command) {
        if (command == null || command.userId() == null || command.userId() <= 0 ||
            command.planId() == null || command.planId() <= 0 || command.startDate() == null ||
            command.endDate() == null || !command.endDate().isAfter(command.startDate()))
            return Result.failure(ApplicationError.validationError("subscription", "Valid userId, planId and date range are required"));
        // Acquiring a write lock on the IAM user serializes concurrent subscription changes.
        if (!iam.lockActiveRentalCompany(command.userId()))
            return Result.failure(ApplicationError.businessRuleViolation("Subscriber eligibility", "Only active rental companies can subscribe"));
        var plan = plans.findById(command.planId());
        if (plan.isEmpty()) return Result.failure(ApplicationError.notFound("SubscriptionPlan", command.planId().toString()));
        if (!plan.get().isActive())
            return Result.failure(ApplicationError.businessRuleViolation("Subscription plan", "The selected plan is inactive"));
        // The server, not client-supplied dates, determines monthly subscription entitlement.
        if (!"MONTHLY".equalsIgnoreCase(plan.get().getBillingCycle()))
            return Result.failure(ApplicationError.businessRuleViolation("Billing cycle", "Unsupported plan billing cycle"));
        Instant now = Instant.now();
        Instant serverEndDate = now.atZone(java.time.ZoneId.of("America/Lima")).plusMonths(1).toInstant();
        if (subscriptions.hasUnexpiredActiveSubscription(command.userId(), now))
            return Result.failure(ApplicationError.conflict("UserSubscription", "The company already has an unexpired ACTIVE subscription"));
        var subscription = new UserSubscription(null, command.userId(), command.planId(),
            now, serverEndDate, SubscriptionStatus.ACTIVE, command.autoRenew());
        return Result.success(subscriptions.save(subscription));
    }
    @Override @Transactional
    public Result<UserSubscription, ApplicationError> handle(CancelUserSubscriptionCommand command) {
        if (command == null || command.subscriptionId() == null || command.subscriptionId() <= 0 ||
            command.actorUserId() == null || command.actorUserId() <= 0)
            return Result.failure(ApplicationError.validationError("subscription", "Valid subscription and actor ids are required"));
        if (!iam.lockActiveRentalCompany(command.actorUserId()))
            return Result.failure(ApplicationError.businessRuleViolation("Subscriber eligibility", "An active rental company is required"));
        var maybe = subscriptions.findById(command.subscriptionId());
        if (maybe.isEmpty()) return Result.failure(ApplicationError.notFound("UserSubscription", command.subscriptionId().toString()));
        var subscription = maybe.get();
        if (!subscription.getUserId().equals(command.actorUserId()))
            return Result.failure(ApplicationError.businessRuleViolation("Subscriber ownership", "Cannot cancel another company's subscription"));
        try { subscription.cancel(); }
        catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.conflict("UserSubscription", ex.getMessage()));
        }
        return Result.success(subscriptions.save(subscription));
    }

    @Override @Transactional
    public Result<UserSubscription, ApplicationError> handle(ChangeUserSubscriptionPlanCommand command) {
        if (command == null || command.subscriptionId() == null || command.subscriptionId() <= 0 ||
            command.newPlanId() == null || command.newPlanId() <= 0 ||
            command.actorUserId() == null || command.actorUserId() <= 0)
            return Result.failure(ApplicationError.validationError("subscription", "Valid subscription, plan and actor ids are required"));

        // This IAM lock also serializes create/cancel/change operations for the same company.
        if (!iam.lockActiveRentalCompany(command.actorUserId()))
            return Result.failure(ApplicationError.businessRuleViolation("Subscriber eligibility", "Only active rental companies can change plans"));

        var maybe = subscriptions.findById(command.subscriptionId());
        if (maybe.isEmpty())
            return Result.failure(ApplicationError.notFound("UserSubscription", command.subscriptionId().toString()));
        var subscription = maybe.get();
        if (!subscription.getUserId().equals(command.actorUserId()))
            return Result.failure(ApplicationError.businessRuleViolation("Subscriber ownership", "Cannot change another company's subscription"));

        var maybePlan = plans.findById(command.newPlanId());
        if (maybePlan.isEmpty())
            return Result.failure(ApplicationError.notFound("SubscriptionPlan", command.newPlanId().toString()));
        if (!maybePlan.get().isActive())
            return Result.failure(ApplicationError.businessRuleViolation("Subscription plan", "The selected plan is inactive"));
        if (!"MONTHLY".equalsIgnoreCase(maybePlan.get().getBillingCycle()))
            return Result.failure(ApplicationError.businessRuleViolation("Billing cycle", "Unsupported plan billing cycle"));
        try {
            subscription.changePlan(command.newPlanId());
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return Result.failure(ApplicationError.conflict("UserSubscription", ex.getMessage()));
        }
        return Result.success(subscriptions.save(subscription));
    }
}

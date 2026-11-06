package com.maquigest.platform.subscriptions.domain.model.commands;

/** Changes the plan of an existing active subscription without renewing its period. */
public record ChangeUserSubscriptionPlanCommand(Long subscriptionId, Long newPlanId, Long actorUserId) { }

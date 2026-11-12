package com.maquigest.platform.subscriptions.domain.model.commands;
public record CancelUserSubscriptionCommand(Long subscriptionId, Long actorUserId) { }

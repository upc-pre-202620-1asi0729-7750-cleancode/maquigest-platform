package com.maquigest.platform.subscriptions.domain.model.commands;
import java.time.Instant;
public record CreateUserSubscriptionCommand(Long userId, Long planId,
                                             Instant startDate, Instant endDate,
                                             boolean autoRenew) { }

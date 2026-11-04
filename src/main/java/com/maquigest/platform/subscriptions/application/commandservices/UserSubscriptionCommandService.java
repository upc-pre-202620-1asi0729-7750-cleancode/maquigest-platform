package com.maquigest.platform.subscriptions.application.commandservices;
import com.maquigest.platform.shared.application.result.*;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
public interface UserSubscriptionCommandService {
    Result<UserSubscription, ApplicationError> handle(CreateUserSubscriptionCommand command);
    Result<UserSubscription, ApplicationError> handle(CancelUserSubscriptionCommand command);
}

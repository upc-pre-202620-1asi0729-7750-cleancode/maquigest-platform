package com.maquigest.platform.subscriptions.application.commandservices;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
public interface SubscriptionCheckoutCommandService {
    record CheckoutResult(String reference,CheckoutStatus status,String checkoutUrl,Long subscriptionId) {}
    Result<CheckoutResult, ApplicationError> handle(CreateSubscriptionCheckoutCommand command);
    Result<CheckoutResult, ApplicationError> handle(RefreshSubscriptionCheckoutCommand command);
    Result<Boolean, ApplicationError> handle(ProcessSubscriptionWebhookCommand command);
}

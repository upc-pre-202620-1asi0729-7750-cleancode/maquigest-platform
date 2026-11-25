package com.maquigest.platform.subscriptions.application.commandservices;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
public interface SubscriptionCheckoutCommandService {
    record CheckoutResult(String reference,CheckoutStatus status,String checkoutUrl,Long subscriptionId) {}
    CheckoutResult handle(CreateSubscriptionCheckoutCommand command);
    CheckoutResult handle(RefreshSubscriptionCheckoutCommand command);
    void handle(ProcessSubscriptionWebhookCommand command);
}

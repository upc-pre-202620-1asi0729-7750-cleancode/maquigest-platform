package com.maquigest.platform.subscriptions.interfaces.rest.transform;
import com.maquigest.platform.subscriptions.application.commandservices.SubscriptionCheckoutCommandService.CheckoutResult;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.SubscriptionCheckoutResource;
public final class SubscriptionCheckoutResourceAssembler {
    private SubscriptionCheckoutResourceAssembler() {}
    public static SubscriptionCheckoutResource toResource(CheckoutResult r) {return new SubscriptionCheckoutResource(r.reference(),r.status().name(),r.checkoutUrl(),r.subscriptionId());}
}

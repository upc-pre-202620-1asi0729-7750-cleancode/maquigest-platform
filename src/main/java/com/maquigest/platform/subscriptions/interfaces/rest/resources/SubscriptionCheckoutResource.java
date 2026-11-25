package com.maquigest.platform.subscriptions.interfaces.rest.resources;
public record SubscriptionCheckoutResource(String reference,String status,String checkoutUrl,Long subscriptionId) {}

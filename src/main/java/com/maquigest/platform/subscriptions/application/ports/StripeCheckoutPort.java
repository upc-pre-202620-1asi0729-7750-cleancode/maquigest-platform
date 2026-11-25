package com.maquigest.platform.subscriptions.application.ports;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import java.util.Optional;
public interface StripeCheckoutPort {
    record Session(String id,String reference,long amountMinor,String currency,String status,String paymentStatus,boolean testMode,String mode,String url) {}
    boolean isEnabled();
    Session create(SubscriptionCheckout checkout);
    Session retrieve(String sessionId);
    Optional<String> verifyWebhook(byte[] payload,String signature);
}

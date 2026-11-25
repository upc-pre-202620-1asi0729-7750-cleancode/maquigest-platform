package com.maquigest.platform.subscriptions.domain.model.aggregates;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.UserId;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;
@Getter
public final class SubscriptionCheckout extends AbstractDomainAggregateRoot<SubscriptionCheckout> {
    private final Long id;
    private final String reference;
    private final UserId userId;
    private final Long planId;
    private final long amountMinor;
    private final Instant expiresAt;
    private String sessionId;
    private CheckoutStatus status;
    private Long subscriptionId;
    private SubscriptionCheckout(Long id,String reference,Long userId,Long planId,long amountMinor,Instant expiresAt,
            String sessionId,CheckoutStatus status,Long subscriptionId) {
        if (id != null && id <= 0 || userId == null || userId <= 0 || planId == null || planId <= 0 || amountMinor <= 0 || expiresAt == null)
            throw new IllegalArgumentException("Invalid checkout");
        UUID.fromString(reference);
        if (sessionId != null && !sessionId.matches("cs_test_[A-Za-z0-9_]+")) throw new IllegalArgumentException("Invalid test session");
        if (status == null || (status == CheckoutStatus.PAID) != (subscriptionId != null) ||
                subscriptionId != null && (subscriptionId <= 0 || sessionId == null)) throw new IllegalArgumentException("Invalid checkout status");
        this.id=id;this.reference=reference;this.userId=new UserId(userId);this.planId=planId;this.amountMinor=amountMinor;
        this.expiresAt=expiresAt;this.sessionId=sessionId;this.status=status;this.subscriptionId=subscriptionId;
    }
    public Long getUserId() { return userId.value(); }
    public static SubscriptionCheckout issue(Long userId,Long planId,long amount,Instant expiresAt) {
        return new SubscriptionCheckout(null,UUID.randomUUID().toString(),userId,planId,amount,expiresAt,null,CheckoutStatus.PENDING,null);
    }
    public static SubscriptionCheckout reconstitute(Long id,String reference,Long userId,Long planId,long amount,Instant expiresAt,
            String sessionId,CheckoutStatus status,Long subscriptionId) {
        if (id == null) throw new IllegalArgumentException("Missing persisted identity");
        return new SubscriptionCheckout(id,reference,userId,planId,amount,expiresAt,sessionId,status,subscriptionId);
    }
    public void bindSession(String session) {
        if (session == null || !session.matches("cs_test_[A-Za-z0-9_]+") || sessionId != null && !sessionId.equals(session))
            throw new IllegalArgumentException("Checkout session mismatch");
        sessionId=session;
    }
    public void markPaid(Long subscription) {
        if (status != CheckoutStatus.PENDING || sessionId == null || subscription == null || subscription <= 0)
            throw new IllegalStateException("Checkout cannot be fulfilled");
        status=CheckoutStatus.PAID;subscriptionId=subscription;
    }
    public void expire() {
        if (status != CheckoutStatus.PENDING || sessionId == null) throw new IllegalStateException("Checkout cannot expire");
        status=CheckoutStatus.EXPIRED;
    }
}

package com.maquigest.platform.subscriptions.application.internal.commandservices;
import com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure;
import static com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure.Code.*;
import com.maquigest.platform.subscriptions.application.ports.*;
import com.maquigest.platform.subscriptions.domain.model.aggregates.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.*;
import com.maquigest.platform.subscriptions.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
@Service
public class SubscriptionCheckoutTransactions {
    private final SubscriptionCheckoutRepository checkouts;
    private final SubscriptionPlanRepository plans;
    private final UserSubscriptionRepository subscriptions;
    private final IamSubscriberPort iam;
    public SubscriptionCheckoutTransactions(SubscriptionCheckoutRepository checkouts,SubscriptionPlanRepository plans,UserSubscriptionRepository subscriptions,IamSubscriberPort iam) {
        this.checkouts=checkouts;this.plans=plans;this.subscriptions=subscriptions;this.iam=iam;
    }
    private void lock(Long user) {if (user==null || !iam.lockActiveRentalCompany(user)) throw new CheckoutFailure(FORBIDDEN);}
    @Transactional public SubscriptionCheckout reserve(Long user,Long planId) {
        lock(user);
        var pending=checkouts.findPendingByUserId(user);
        if (pending.isPresent()) return pending.get();
        var now=Instant.now();
        if (subscriptions.hasUnexpiredActiveSubscription(user,now)) throw new CheckoutFailure(CONFLICT);
        var plan=plans.findById(planId).orElseThrow(()->new CheckoutFailure(NOT_FOUND));
        if (!plan.isActive() || !"MONTHLY".equalsIgnoreCase(plan.getBillingCycle()) || !"PEN".equals(plan.getPriceCurrency())) throw new CheckoutFailure(INVALID);
        long amount;
        try {amount=plan.getPriceAmount().movePointRight(2).longValueExact();} catch (ArithmeticException e) {throw new CheckoutFailure(INVALID);}
        if (amount<=0 || amount>99999999) throw new CheckoutFailure(INVALID);
        return checkouts.save(SubscriptionCheckout.issue(user,planId,amount,now.plusSeconds(7200).truncatedTo(java.time.temporal.ChronoUnit.SECONDS)));
    }
    @Transactional(readOnly=true) public SubscriptionCheckout find(String ref) {return checkouts.findByReference(ref).orElseThrow(()->new CheckoutFailure(NOT_FOUND));}
    @Transactional public SubscriptionCheckout reconcile(Long user,StripeCheckoutPort.Session session) {
        lock(user);
        var c=checkouts.findByReferenceForUpdate(session.reference()).orElseThrow(()->new CheckoutFailure(NOT_FOUND));
        if (!c.getUserId().equals(user) || !session.testMode() || !"payment".equals(session.mode()) || !"pen".equals(session.currency()) || c.getAmountMinor()!=session.amountMinor()) throw new CheckoutFailure(INVALID);
        try {c.bindSession(session.id());} catch (IllegalArgumentException e) {throw new CheckoutFailure(INVALID);}
        if (c.getStatus()!=CheckoutStatus.PENDING) return c;
        if ("complete".equals(session.status()) && "paid".equals(session.paymentStatus())) {
            var now=Instant.now();
            if (subscriptions.hasUnexpiredActiveSubscription(user,now)) throw new CheckoutFailure(CONFLICT);
            var end=now.atZone(ZoneId.of("America/Lima")).plusMonths(1).toInstant();
            var subscription=subscriptions.save(new UserSubscription(null,user,c.getPlanId(),now,end,SubscriptionStatus.ACTIVE,false));
            c.markPaid(subscription.getId());
        } else if ("expired".equals(session.status()) && "unpaid".equals(session.paymentStatus())) {c.expire();}
        return checkouts.save(c);
    }
}

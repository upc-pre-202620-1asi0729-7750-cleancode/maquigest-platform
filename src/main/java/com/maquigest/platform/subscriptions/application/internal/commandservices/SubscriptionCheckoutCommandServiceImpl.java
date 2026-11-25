package com.maquigest.platform.subscriptions.application.internal.commandservices;
import com.maquigest.platform.subscriptions.application.commandservices.*;
import static com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure.Code.*;
import com.maquigest.platform.subscriptions.application.ports.StripeCheckoutPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import org.springframework.stereotype.Service;
import java.time.Instant;
@Service
public class SubscriptionCheckoutCommandServiceImpl implements SubscriptionCheckoutCommandService {
    private final SubscriptionCheckoutTransactions tx;
    private final StripeCheckoutPort stripe;
    public SubscriptionCheckoutCommandServiceImpl(SubscriptionCheckoutTransactions tx,StripeCheckoutPort stripe) {this.tx=tx;this.stripe=stripe;}
    private void enabled() {if(!stripe.isEnabled()) throw new CheckoutFailure(UNAVAILABLE);}
    private CheckoutResult result(SubscriptionCheckout c,String url) {return new CheckoutResult(c.getReference(),c.getStatus(),url,c.getSubscriptionId());}
    public CheckoutResult handle(CreateSubscriptionCheckoutCommand command) {
        enabled();
        if (command==null || command.planId()==null || command.planId()<=0) throw new CheckoutFailure(INVALID);
        for (int i=0;i<2;i++) {
            var c=tx.reserve(command.userId(),command.planId());
            StripeCheckoutPort.Session session;
            if (c.getSessionId()==null) {
                // Reserve first, then use the same durable key on every retry. Never issue an uncertain second charge.
                if (c.getExpiresAt().isBefore(Instant.now().plusSeconds(1800))) throw new CheckoutFailure(CONFLICT);
                session=stripe.create(c);
            } else {session=stripe.retrieve(c.getSessionId());}
            if (!c.getReference().equals(session.reference())) throw new CheckoutFailure(INVALID);
            var updated=tx.reconcile(c.getUserId(),session);
            if (updated.getStatus()==CheckoutStatus.EXPIRED) continue;
            if (!updated.getPlanId().equals(command.planId())) throw new CheckoutFailure(CONFLICT);
            return result(updated,updated.getStatus()==CheckoutStatus.PENDING && "open".equals(session.status())?session.url():null);
        }
        throw new CheckoutFailure(CONFLICT);
    }
    public CheckoutResult handle(RefreshSubscriptionCheckoutCommand command) {
        enabled();
        if(command==null || command.reference()==null) throw new CheckoutFailure(INVALID);
        var c=tx.find(command.reference());
        if (!c.getUserId().equals(command.userId())) throw new CheckoutFailure(NOT_FOUND);
        if (c.getStatus()!=CheckoutStatus.PENDING || c.getSessionId()==null) return result(c,null);
        var session=stripe.retrieve(c.getSessionId());
        if (!c.getReference().equals(session.reference())) throw new CheckoutFailure(INVALID);
        return result(tx.reconcile(c.getUserId(),session),null);
    }
    public void handleWebhook(byte[] payload,String signature) {
        enabled();
        stripe.verifyWebhook(payload,signature).ifPresent(id->{
            var session=stripe.retrieve(id);
            SubscriptionCheckout c;
            try {c=tx.find(session.reference());} catch(CheckoutFailure e) {if(e.code()==NOT_FOUND)return;throw e;}
            tx.reconcile(c.getUserId(),session);
        });
    }
}

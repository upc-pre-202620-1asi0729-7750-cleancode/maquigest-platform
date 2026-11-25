package com.maquigest.platform.rentals.application.internal.eventhandlers;

import com.maquigest.platform.rentals.application.internal.commandservices.RentalEmailDeliveryTransactions;
import com.maquigest.platform.rentals.domain.model.events.RentalNotificationEmailRequestedEvent;
import com.maquigest.platform.shared.application.email.EmailGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** External email never participates in the rental transaction. */
@Service
@Slf4j
public class RentalEmailNotificationEventHandler {
    private final EmailGateway gateway;
    private final RentalEmailDeliveryTransactions transactions;

    public RentalEmailNotificationEventHandler(EmailGateway gateway, RentalEmailDeliveryTransactions transactions) {
        this.gateway = gateway;
        this.transactions = transactions;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RentalNotificationEmailRequestedEvent event) {
        if (!gateway.isEnabled()) return;
        boolean accepted = false;
        try {
            var message = transactions.prepare(event);
            if (message.isEmpty()) return;
            gateway.send(message.get());
            accepted = true;
        } catch (RuntimeException exception) {
            log.warn("Email delivery failed for rental notification {} ({})", event.notificationId(),
                    exception.getClass().getSimpleName());
        }
        try {
            transactions.finish(event, accepted);
        } catch (RuntimeException exception) {
            // The rental is committed; never report a rollback or repeat an uncertain send.
            log.error("Could not persist email outcome for rental notification {} ({})", event.notificationId(),
                    exception.getClass().getSimpleName());
        }
    }
}

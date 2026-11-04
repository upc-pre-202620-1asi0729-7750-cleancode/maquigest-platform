package com.maquigest.platform.rentals.infrastructure.events;
import com.maquigest.platform.rentals.domain.model.events.*;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
/** Forward in-process integration notifications only AFTER the local DB commits. */
@Component
public class RentalIntegrationEventBridge {
    private final ApplicationEventPublisher publisher;
    public RentalIntegrationEventBridge(ApplicationEventPublisher publisher){this.publisher=publisher;}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void approved(RentalRequestApprovedEvent event) {
        publisher.publishEvent(new RentalIntegrationEvent("RENTAL_REQUEST_APPROVED",event.requestId(),event.equipmentId(),Instant.now()));
    }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void activated(RentalActivatedEvent event) {
        publisher.publishEvent(new RentalIntegrationEvent("RENTAL_ACTIVATED",event.rentalId(),event.equipmentId(),Instant.now()));
    }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void completed(RentalCompletedEvent event) {
        publisher.publishEvent(new RentalIntegrationEvent("RENTAL_COMPLETED",event.rentalId(),event.equipmentId(),Instant.now()));
    }
}

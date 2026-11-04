package com.maquigest.platform.profiles.infrastructure.events;
import com.maquigest.platform.profiles.domain.model.events.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.Instant;
/** Translate domain events into post-commit events for other BC adapters. */
@Component
public class CompanyProfileIntegrationEventBridge {
    private final ApplicationEventPublisher publisher;
    public CompanyProfileIntegrationEventBridge(ApplicationEventPublisher publisher){this.publisher=publisher;}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void created(CompanyProfileCreatedEvent e){
        publisher.publishEvent(new CompanyProfileIntegrationEvent("company-profile.created",e.profileId(),e.userId(),Instant.now()));
    }
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void updated(CompanyProfileUpdatedEvent e){
        publisher.publishEvent(new CompanyProfileIntegrationEvent("company-profile.updated",e.profileId(),e.userId(),Instant.now()));
    }
}

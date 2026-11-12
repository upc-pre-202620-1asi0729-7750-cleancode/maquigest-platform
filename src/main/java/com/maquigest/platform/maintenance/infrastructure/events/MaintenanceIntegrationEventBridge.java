package com.maquigest.platform.maintenance.infrastructure.events;
import com.maquigest.platform.maintenance.interfaces.events.MaintenanceIntegrationEvent;
import com.maquigest.platform.maintenance.domain.model.events.*;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
/** In-process AFTER_COMMIT event publication. This is not a durable message broker or outbox. */
@Component
public class MaintenanceIntegrationEventBridge {
    private final ApplicationEventPublisher publisher;
    public MaintenanceIntegrationEventBridge(ApplicationEventPublisher publisher){this.publisher=publisher;}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void reported(IncidentReportedEvent e){publisher.publishEvent(new MaintenanceIntegrationEvent("INCIDENT_REPORTED",null,e.equipmentId(),Instant.now()));}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void resolved(IncidentResolvedEvent e){publisher.publishEvent(new MaintenanceIntegrationEvent("INCIDENT_RESOLVED",e.incidentId(),e.equipmentId(),Instant.now()));}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void created(MaintenanceCreatedEvent e){publisher.publishEvent(new MaintenanceIntegrationEvent("MAINTENANCE_CREATED",null,e.equipmentId(),Instant.now()));}
    @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT)
    public void updated(MaintenanceStatusChangedEvent e){publisher.publishEvent(new MaintenanceIntegrationEvent("MAINTENANCE_STATUS_CHANGED",e.maintenanceId(),e.equipmentId(),Instant.now()));}
}

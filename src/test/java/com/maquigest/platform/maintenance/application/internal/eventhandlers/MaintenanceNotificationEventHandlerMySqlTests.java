package com.maquigest.platform.maintenance.application.internal.eventhandlers;

import com.maquigest.platform.maintenance.domain.model.events.MaintenanceNotificationRequestedEvent;
import com.maquigest.platform.maintenance.domain.repositories.NotificationRepository;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.notificationPersistenceTests", matches = "true")
class MaintenanceNotificationEventHandlerMySqlTests {
    @Autowired private NotificationRepository repository;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private DataSource dataSource;
    @Test void persistsDistinctFactsAndDeduplicatesRedelivery() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertEquals(System.getProperty("maquigest.notificationExpectedDb"), connection.getCatalog());
        }
        var first = new MaintenanceNotificationRequestedEvent(90301L, 90302L, NotificationType.MAINTENANCE_SCHEDULED);
        var second = new MaintenanceNotificationRequestedEvent(90301L, 90302L, NotificationType.MAINTENANCE_COMPLETED);
        var third = new MaintenanceNotificationRequestedEvent(90301L, 90302L, NotificationType.MAINTENANCE_COMPLETED);
        var recipient = new NotificationRecipientId(90302L);
        var tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> { publisher.publishEvent(first); publisher.publishEvent(second); });
        tx.executeWithoutResult(status -> { publisher.publishEvent(second); publisher.publishEvent(third); });
        assertEquals(3, repository.findAllByRecipientId(recipient).size());
        assertEquals(NotificationType.MAINTENANCE_SCHEDULED, repository.findByRecipientIdAndFactId(recipient, first.factId()).orElseThrow().getType());
        var rollback = new MaintenanceNotificationRequestedEvent(90301L, 90302L, NotificationType.MAINTENANCE_COMPLETED);
        tx.executeWithoutResult(status -> { publisher.publishEvent(rollback); status.setRollbackOnly(); });
        assertTrue(repository.findByRecipientIdAndFactId(recipient, rollback.factId()).isEmpty());
    }
    @Autowired private com.maquigest.platform.maintenance.domain.repositories.MaintenanceRepository maintenance;
    @Autowired private com.maquigest.platform.maintenance.domain.repositories.IncidentRepository incidents;
    @Test void actualMaintenanceAndIncidentTransitionsProduceNotifications() {
        var tx = new TransactionTemplate(manager);
        var owner = 90401L;
        var at = java.time.Instant.now();
        var mid = tx.execute(status -> maintenance.save(com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance.register(90402L, owner, at, "Preventive", MaintenanceStatus.SCHEDULED)).getId());
        tx.executeWithoutResult(status -> {
            var item = maintenance.findById(mid).orElseThrow();
            item.changeStatus(MaintenanceStatus.COMPLETED, "Technician", "Done", at);
            maintenance.save(item);
        });
        var iid = tx.execute(status -> incidents.save(com.maquigest.platform.maintenance.domain.model.aggregates.Incident.report(90402L, owner, "Test incident", false, at)).getId());
        tx.executeWithoutResult(status -> {
            var item = incidents.findById(iid).orElseThrow();
            item.resolve(at.plusSeconds(1));
            incidents.save(item);
        });
        var notifications = repository.findAllByRecipientId(new NotificationRecipientId(owner));
        assertEquals(4, notifications.size());
        for (var type : NotificationType.values()) {
            var item = notifications.stream().filter(n -> n.getType() == type).findFirst().orElseThrow();
            assertEquals(type.name().startsWith("INCIDENT") ? iid : mid, item.getReferenceId().referenceId());
        }
    }
}

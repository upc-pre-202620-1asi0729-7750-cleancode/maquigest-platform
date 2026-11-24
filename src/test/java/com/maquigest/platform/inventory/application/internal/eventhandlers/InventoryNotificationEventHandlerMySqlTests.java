package com.maquigest.platform.inventory.application.internal.eventhandlers;

import com.maquigest.platform.inventory.domain.model.events.*;
import com.maquigest.platform.inventory.domain.repositories.NotificationRepository;
import com.maquigest.platform.inventory.domain.model.valueobjects.*;
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
class InventoryNotificationEventHandlerMySqlTests {
    @Autowired private NotificationRepository repository;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private DataSource dataSource;
    @Test void persistsDistinctFactsAndDeduplicatesRedelivery() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertEquals(System.getProperty("maquigest.notificationExpectedDb"), connection.getCatalog());
        }
        var first = new EquipmentCreatedEvent(90101L, 90102L);
        var second = new EquipmentStatusChangedEvent(90101L, 90102L);
        var third = new EquipmentStatusChangedEvent(90101L, 90102L);
        var recipient = new NotificationRecipientId(90102L);
        var tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> { publisher.publishEvent(first); publisher.publishEvent(second); });
        tx.executeWithoutResult(status -> { publisher.publishEvent(second); publisher.publishEvent(third); });
        assertEquals(3, repository.findAllByRecipientId(recipient).size());
        assertEquals(NotificationType.EQUIPMENT_CREATED, repository.findByRecipientIdAndFactId(recipient, first.factId()).orElseThrow().getType());
        assertEquals(NotificationType.EQUIPMENT_STATUS_CHANGED, repository.findByRecipientIdAndFactId(recipient, third.factId()).orElseThrow().getType());
        var rollback = new EquipmentStatusChangedEvent(90101L, 90102L);
        tx.executeWithoutResult(status -> { publisher.publishEvent(rollback); status.setRollbackOnly(); });
        assertTrue(repository.findByRecipientIdAndFactId(recipient, rollback.factId()).isEmpty());
    }
    @Autowired private com.maquigest.platform.inventory.domain.repositories.EquipmentRepository equipment;
    @Autowired private com.maquigest.platform.inventory.domain.repositories.EquipmentCategoryRepository categories;
    @Test void actualEquipmentCreationAndStatusChangesProduceNotifications() {
        var tx = new TransactionTemplate(manager);
        var categoryId = categories.findAll().getFirst().getId();
        var id = tx.execute(status -> equipment.save(new com.maquigest.platform.inventory.domain.model.aggregates.Equipment(
                new com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand(
                90201L, "NOTIF-TEST", "Equipment", "Test equipment", categoryId, "Lima",
                java.math.BigDecimal.valueOf(100), java.math.BigDecimal.valueOf(500)))).getId());
        tx.executeWithoutResult(status -> {
            var item = equipment.findByIdForUpdate(id).orElseThrow();
            item.changeOperationalStatus(com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus.MAINTENANCE);
            equipment.save(item);
        });
        tx.executeWithoutResult(status -> {
            var item = equipment.findByIdForUpdate(id).orElseThrow();
            item.changeOperationalStatus(com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus.AVAILABLE);
            equipment.save(item);
        });
        var notifications = repository.findAllByRecipientId(new NotificationRecipientId(90201L));
        assertEquals(3, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getReferenceId().referenceId().equals(id)));
        assertEquals(2L, notifications.stream().filter(n -> n.getType() == NotificationType.EQUIPMENT_STATUS_CHANGED).count());
    }
}

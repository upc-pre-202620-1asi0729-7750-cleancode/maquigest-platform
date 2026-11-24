package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.rentals.application.commandservices.NotificationCommandService;
import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.NotificationPersistenceRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.notificationPersistenceTests", matches = "true")
class NotificationPersistenceMySqlTests {
    @Autowired private NotificationCommandService commands;
    @Autowired private NotificationRepository repository;
    @Autowired private NotificationPersistenceRepository jpa;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private DataSource dataSource;

    @DynamicPropertySource
    static void requireIsolatedSchema(DynamicPropertyRegistry registry) {
        var expected = System.getProperty("maquigest.notificationExpectedDb", "");
        var url = System.getenv("SPRING_DATASOURCE_URL");
        if (!expected.matches("maquigest_ddd_notification_[a-z0-9]{8}") || url == null ||
                !url.startsWith("jdbc:mysql://localhost:3306/" + expected + "?")) {
            throw new IllegalStateException("Notification integration tests require an isolated MySQL schema");
        }
        registry.add("spring.datasource.url", () -> url);
    }

    @Test void createsAuditedNotificationAndDeduplicatesSequentialFact() throws Exception {
        var expected = System.getProperty("maquigest.notificationExpectedDb", "");
        assertTrue(expected.matches("maquigest_ddd_notification_[a-z0-9]{8}"));
        try (var connection = dataSource.getConnection()) {
            assertEquals(expected, connection.getCatalog());
        }
        var command = new CreateNotificationCommand(new NotificationRecipientId(10001L),
                new NotificationReferenceId(10002L), NotificationType.RENTAL_REQUEST_CREATED,
                Instant.parse("2026-11-24T12:00:00Z"));
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var id = commands.handle(command).toOptional().orElseThrow();
            assertEquals(id, commands.handle(command).toOptional().orElseThrow());
            var notification = repository.findById(id).orElseThrow();
            assertEquals(NotificationEmailStatus.PENDING, notification.getEmailStatus());
            assertFalse(notification.isRead());
            assertEquals(command.recipientId(), notification.getRecipientId());
            var entity = jpa.findById(id).orElseThrow();
            assertNotNull(entity.getCreatedAt());
            assertNotNull(entity.getUpdatedAt());
            notification.markAsRead(command.occurredAt().plusSeconds(5));
            repository.save(notification);
            assertTrue(repository.findById(id).orElseThrow().isRead());
            status.setRollbackOnly();
        });
        assertTrue(repository.findByRecipientIdAndReferenceIdAndType(command.recipientId(),
                command.referenceId(), command.type()).isEmpty());
    }
}

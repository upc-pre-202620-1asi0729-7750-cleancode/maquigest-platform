package com.maquigest.platform.iam.application.internal.eventhandlers;

import com.maquigest.platform.iam.domain.model.events.IamNotificationRequestedEvent;
import com.maquigest.platform.iam.domain.repositories.NotificationRepository;
import com.maquigest.platform.iam.domain.model.valueobjects.*;
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
class IamNotificationEventHandlerMySqlTests {
    @Autowired private NotificationRepository repository;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private DataSource dataSource;
    @Test void persistsDistinctFactsAndDeduplicatesRedelivery() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertEquals(System.getProperty("maquigest.notificationExpectedDb"), connection.getCatalog());
        }
        var first = new IamNotificationRequestedEvent(90301L, 90302L, NotificationType.ACCOUNT_REGISTERED);
        var second = new IamNotificationRequestedEvent(90301L, 90302L, NotificationType.ACCOUNT_REGISTERED);
        var third = new IamNotificationRequestedEvent(90301L, 90302L, NotificationType.ACCOUNT_REGISTERED);
        var recipient = new NotificationRecipientId(90302L);
        var tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> { publisher.publishEvent(first); publisher.publishEvent(second); });
        tx.executeWithoutResult(status -> { publisher.publishEvent(second); publisher.publishEvent(third); });
        assertEquals(3, repository.findAllByRecipientId(recipient).size());
        assertEquals(NotificationType.ACCOUNT_REGISTERED, repository.findByRecipientIdAndFactId(recipient, first.factId()).orElseThrow().getType());
        var rollback = new IamNotificationRequestedEvent(90301L, 90302L, NotificationType.ACCOUNT_REGISTERED);
        tx.executeWithoutResult(status -> { publisher.publishEvent(rollback); status.setRollbackOnly(); });
        assertTrue(repository.findByRecipientIdAndFactId(recipient, rollback.factId()).isEmpty());
    }
    @Autowired private com.maquigest.platform.iam.application.commandservices.UserCommandService users;
    @Autowired private com.maquigest.platform.iam.domain.repositories.UserRepository userRepository;
    @Test void actualSignUpProducesOneNotificationAndRollbackLeavesNone() {
        var tx = new TransactionTemplate(manager);
        var email = "iam-notification-" + java.util.UUID.randomUUID() + "@example.test";
        var command = new com.maquigest.platform.iam.domain.model.commands.SignUpCommand(
                "Ada", "Tester", email, "MaquiGestTest#2026", "IAM Test SAC", Roles.ROLE_RENTAL_COMPANY);
        var user = users.handle(command).toOptional().orElseThrow();
        var recipient = new NotificationRecipientId(user.getId());
        var notifications = repository.findAllByRecipientId(recipient);
        assertEquals(1, notifications.size());
        assertEquals(NotificationType.ACCOUNT_REGISTERED, notifications.getFirst().getType());
        assertEquals(user.getId(), notifications.getFirst().getReferenceId().referenceId());
        assertEquals(NotificationEmailStatus.NOT_REQUIRED, notifications.getFirst().getEmailStatus());
        tx.executeWithoutResult(status -> userRepository.save(userRepository.findByEmail(email).orElseThrow()));
        assertEquals(1, repository.findAllByRecipientId(recipient).size());
        var rollbackEmail = "iam-rollback-" + java.util.UUID.randomUUID() + "@example.test";
        var rolledBackId = tx.execute(status -> {
            var rolledBack = users.handle(new com.maquigest.platform.iam.domain.model.commands.SignUpCommand(
                    "Ada", "Rollback", rollbackEmail, "MaquiGestTest#2026", "IAM Test SAC", Roles.ROLE_RENTAL_COMPANY))
                    .toOptional().orElseThrow().getId();
            status.setRollbackOnly();
            return rolledBack;
        });
        assertTrue(userRepository.findByEmail(rollbackEmail).isEmpty());
        assertTrue(repository.findAllByRecipientId(new NotificationRecipientId(rolledBackId)).isEmpty());
    }
}

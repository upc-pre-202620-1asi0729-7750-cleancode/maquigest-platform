package com.maquigest.platform.profiles.application.internal.eventhandlers;

import com.maquigest.platform.profiles.domain.model.events.*;
import com.maquigest.platform.profiles.domain.repositories.NotificationRepository;
import com.maquigest.platform.profiles.domain.model.valueobjects.*;
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
class ProfileNotificationEventHandlerMySqlTests {
    @Autowired private NotificationRepository repository;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private DataSource dataSource;
    @Test void persistsDistinctFactsAndDeduplicatesRedelivery() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertEquals(System.getProperty("maquigest.notificationExpectedDb"), connection.getCatalog());
        }
        var first = new CompanyProfileCreatedEvent(90101L, 90102L);
        var second = new CompanyProfileUpdatedEvent(90101L, 90102L);
        var third = new CompanyProfileUpdatedEvent(90101L, 90102L);
        var recipient = new NotificationRecipientId(90102L);
        var tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> { publisher.publishEvent(first); publisher.publishEvent(second); });
        tx.executeWithoutResult(status -> { publisher.publishEvent(second); publisher.publishEvent(third); });
        assertEquals(3, repository.findAllByRecipientId(recipient).size());
        assertEquals(NotificationType.PROFILE_CREATED, repository.findByRecipientIdAndFactId(recipient, first.factId()).orElseThrow().getType());
        assertEquals(NotificationType.PROFILE_UPDATED, repository.findByRecipientIdAndFactId(recipient, third.factId()).orElseThrow().getType());
        var rollback = new CompanyProfileUpdatedEvent(90101L, 90102L);
        tx.executeWithoutResult(status -> { publisher.publishEvent(rollback); status.setRollbackOnly(); });
        assertTrue(repository.findByRecipientIdAndFactId(recipient, rollback.factId()).isEmpty());
    }
    @Autowired private com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository profiles;
    @Test void actualProfileCreationAndUpdateProduceNotifications() {
        var tx = new TransactionTemplate(manager);
        var address = com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress.empty();
        var id = tx.execute(status -> profiles.save(new com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile(
                new com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand(
                90201L, "Test", "Profile", "profile-event@example.test", "", "Company", address))).getId());
        tx.executeWithoutResult(status -> {
            var profile = profiles.findById(id).orElseThrow();
            profile.update(new com.maquigest.platform.profiles.domain.model.commands.UpdateCompanyProfileCommand(
                    id, 90201L, "Test", "Updated", "profile-event@example.test", "", "Company", address));
            profiles.save(profile);
        });
        var notifications = repository.findAllByRecipientId(new NotificationRecipientId(90201L));
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getReferenceId().referenceId().equals(id)));
        assertTrue(notifications.stream().anyMatch(n -> n.getType() == NotificationType.PROFILE_CREATED));
        assertTrue(notifications.stream().anyMatch(n -> n.getType() == NotificationType.PROFILE_UPDATED));
    }
}

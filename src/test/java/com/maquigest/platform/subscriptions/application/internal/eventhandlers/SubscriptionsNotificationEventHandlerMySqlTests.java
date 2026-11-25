package com.maquigest.platform.subscriptions.application.internal.eventhandlers;

import com.maquigest.platform.subscriptions.domain.model.events.SubscriptionsNotificationRequestedEvent;
import com.maquigest.platform.subscriptions.domain.repositories.NotificationRepository;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.*;
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
class SubscriptionsNotificationEventHandlerMySqlTests {
    @Autowired private NotificationRepository repository;
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private PlatformTransactionManager manager;
    @Autowired private DataSource dataSource;
    @Test void persistsDistinctFactsAndDeduplicatesRedelivery() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertEquals(System.getProperty("maquigest.notificationExpectedDb"), connection.getCatalog());
        }
        var first = new SubscriptionsNotificationRequestedEvent(90301L, 90302L, NotificationType.SUBSCRIPTION_ACTIVATED);
        var second = new SubscriptionsNotificationRequestedEvent(90301L, 90302L, NotificationType.SUBSCRIPTION_CANCELLED);
        var third = new SubscriptionsNotificationRequestedEvent(90301L, 90302L, NotificationType.SUBSCRIPTION_CANCELLED);
        var recipient = new NotificationRecipientId(90302L);
        var tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> { publisher.publishEvent(first); publisher.publishEvent(second); });
        tx.executeWithoutResult(status -> { publisher.publishEvent(second); publisher.publishEvent(third); });
        assertEquals(3, repository.findAllByRecipientId(recipient).size());
        assertEquals(NotificationType.SUBSCRIPTION_ACTIVATED, repository.findByRecipientIdAndFactId(recipient, first.factId()).orElseThrow().getType());
        var rollback = new SubscriptionsNotificationRequestedEvent(90301L, 90302L, NotificationType.SUBSCRIPTION_CANCELLED);
        tx.executeWithoutResult(status -> { publisher.publishEvent(rollback); status.setRollbackOnly(); });
        assertTrue(repository.findByRecipientIdAndFactId(recipient, rollback.factId()).isEmpty());
    }
    @Autowired private com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository subscriptions;
    @Test void actualSubscriptionActivationAndCancellationProduceNotifications() {
        var tx = new TransactionTemplate(manager);
        var owner = 90501L;
        var at = java.time.Instant.now();
        var id = tx.execute(status -> subscriptions.save(new com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription(null, owner, 1L, at.minusSeconds(60), at.plusSeconds(86400), SubscriptionStatus.ACTIVE, false)).getId());
        tx.executeWithoutResult(status -> {
            var item = subscriptions.findById(id).orElseThrow();
            item.cancel();
            subscriptions.save(item);
        });
        var notifications = repository.findAllByRecipientId(new NotificationRecipientId(owner));
        assertEquals(2, notifications.size());
        assertTrue(notifications.stream().allMatch(n -> n.getReferenceId().referenceId().equals(id)));
        assertEquals(java.util.Set.of(NotificationType.SUBSCRIPTION_ACTIVATED, NotificationType.SUBSCRIPTION_CANCELLED),
                notifications.stream().map(n -> n.getType()).collect(java.util.stream.Collectors.toSet()));
    }
}

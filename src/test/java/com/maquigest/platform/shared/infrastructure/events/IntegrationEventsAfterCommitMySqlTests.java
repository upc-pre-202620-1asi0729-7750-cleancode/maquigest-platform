package com.maquigest.platform.shared.infrastructure.events;

import com.maquigest.platform.maintenance.domain.model.events.MaintenanceCreatedEvent;
import com.maquigest.platform.maintenance.interfaces.events.MaintenanceIntegrationEvent;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileCreatedEvent;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileUpdatedEvent;
import com.maquigest.platform.profiles.interfaces.events.CompanyProfileIntegrationEvent;
import com.maquigest.platform.rentals.domain.model.events.RentalActivatedEvent;
import com.maquigest.platform.rentals.interfaces.events.RentalIntegrationEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

/** Real Spring transaction dispatch: domain -> AFTER_COMMIT bridge -> public local event. */
@SpringBootTest
@Import(IntegrationEventsAfterCommitMySqlTests.ProbeConfiguration.class)
class IntegrationEventsAfterCommitMySqlTests {
    @Autowired private ApplicationEventPublisher publisher;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private EventProbe probe;

    @BeforeEach void reset() { probe.events.clear(); }

    @Test void singleCommittedDomainEventProducesOneIntegrationEventAfterCommit() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(new CompanyProfileCreatedEvent(9L, 3L));
            assertEquals(0, probe.events.size(), "No integration publication before commit");
        });
        assertEquals(1, probe.events.size());
        assertInstanceOf(CompanyProfileIntegrationEvent.class, probe.events.get(0));
        var event = (CompanyProfileIntegrationEvent) probe.events.get(0);
        assertEquals("company-profile.created", event.eventName());
        assertEquals(9L, event.profileId());
        assertEquals(3L, event.userId());
    }

    @Test void eventOutsideAnyTransactionDoesNotNotifyBridge() {
        publisher.publishEvent(new RentalActivatedEvent(12L, 31L));
        assertTrue(probe.events.isEmpty(), "AFTER_COMMIT bridge must not use fallbackExecution");
    }

    @Test void rolledBackDomainEventsProduceNoIntegrationNotifications() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(new CompanyProfileUpdatedEvent(9L, 3L));
            publisher.publishEvent(new RentalActivatedEvent(12L, 31L));
            publisher.publishEvent(new MaintenanceCreatedEvent(31L, "SCHEDULED"));
            assertTrue(probe.events.isEmpty());
            status.setRollbackOnly();
        });
        assertTrue(probe.events.isEmpty(), "A rolled back write must not be observed");
    }

    @Test void committedEventsFromAllThreeContextsAreForwardedInOrder() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            publisher.publishEvent(new CompanyProfileUpdatedEvent(9L, 3L));
            publisher.publishEvent(new RentalActivatedEvent(12L, 31L));
            publisher.publishEvent(new MaintenanceCreatedEvent(31L, "SCHEDULED"));
            assertEquals(0, probe.events.size());
        });
        List<Object> snapshot = List.copyOf(probe.events);
        assertEquals(3, snapshot.size());
        assertInstanceOf(CompanyProfileIntegrationEvent.class, snapshot.get(0));
        assertInstanceOf(RentalIntegrationEvent.class, snapshot.get(1));
        assertInstanceOf(MaintenanceIntegrationEvent.class, snapshot.get(2));
        assertEquals("company-profile.updated", ((CompanyProfileIntegrationEvent) snapshot.get(0)).eventName());
        assertEquals("RENTAL_ACTIVATED", ((RentalIntegrationEvent) snapshot.get(1)).eventType());
        assertEquals("MAINTENANCE_CREATED", ((MaintenanceIntegrationEvent) snapshot.get(2)).type());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ProbeConfiguration {
        @Bean EventProbe eventProbe() { return new EventProbe(); }
    }

    static final class EventProbe {
        final CopyOnWriteArrayList<Object> events = new CopyOnWriteArrayList<>();
        @EventListener public void profile(CompanyProfileIntegrationEvent e) { events.add(e); }
        @EventListener public void rental(RentalIntegrationEvent e) { events.add(e); }
        @EventListener public void maintenance(MaintenanceIntegrationEvent e) { events.add(e); }
    }
}

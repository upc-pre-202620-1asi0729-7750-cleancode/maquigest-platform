package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.profiles.application.internal.eventhandlers.CompanyProfileIntegrationEventBridge;
import com.maquigest.platform.rentals.application.internal.eventhandlers.RentalIntegrationEventBridge;
import com.maquigest.platform.maintenance.application.internal.eventhandlers.MaintenanceIntegrationEventBridge;
import com.maquigest.platform.profiles.interfaces.events.CompanyProfileIntegrationEvent;
import com.maquigest.platform.rentals.interfaces.events.RentalIntegrationEvent;
import com.maquigest.platform.maintenance.interfaces.events.MaintenanceIntegrationEvent;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileCreatedEvent;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileUpdatedEvent;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestApprovedEvent;
import com.maquigest.platform.rentals.domain.model.events.RentalActivatedEvent;
import com.maquigest.platform.rentals.domain.model.events.RentalCompletedEvent;
import com.maquigest.platform.maintenance.domain.model.events.IncidentReportedEvent;
import com.maquigest.platform.maintenance.domain.model.events.IncidentResolvedEvent;
import com.maquigest.platform.maintenance.domain.model.events.MaintenanceCreatedEvent;
import com.maquigest.platform.maintenance.domain.model.events.MaintenanceStatusChangedEvent;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import static org.mockito.Mockito.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import static org.junit.jupiter.api.Assertions.*;

/** Published DTOs belong to owning bounded contexts, not infrastructure adapters. */
class PublishedIntegrationEventBoundaryTests {
    @Test void contractsLiveInOwningInterfacesEventsPackage() {
        assertEquals("com.maquigest.platform.profiles.interfaces.events", CompanyProfileIntegrationEvent.class.getPackageName());
        assertEquals("com.maquigest.platform.rentals.interfaces.events", RentalIntegrationEvent.class.getPackageName());
        assertEquals("com.maquigest.platform.maintenance.interfaces.events", MaintenanceIntegrationEvent.class.getPackageName());
        assertEquals("company-profile.created", new CompanyProfileIntegrationEvent(
            "company-profile.created", 1L, 2L, Instant.EPOCH).eventName());
        assertEquals("RENTAL_COMPLETED", new RentalIntegrationEvent(
            "RENTAL_COMPLETED", 1L, 2L, Instant.EPOCH).eventType());
        assertEquals("MAINTENANCE_CREATED", new MaintenanceIntegrationEvent(
            "MAINTENANCE_CREATED", null, 2L, Instant.EPOCH).type());
    }

    @Test void everyBridgeHandlerWaitsForCommit() {
        var expected = Map.of(CompanyProfileIntegrationEventBridge.class, 2,
                RentalIntegrationEventBridge.class, 3,
                MaintenanceIntegrationEventBridge.class, 4);
        for (var entry : expected.entrySet()) {
            Class<?> bridge = entry.getKey();
            int count = 0;
            for (Method method : bridge.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(TransactionalEventListener.class)) continue;
                assertEquals(TransactionPhase.AFTER_COMMIT,
                    method.getAnnotation(TransactionalEventListener.class).phase(), method.toString());
                assertFalse(method.getAnnotation(TransactionalEventListener.class).fallbackExecution(),
                    "Bridge must not publish outside a transaction: " + method);
                count++;
            }
            assertEquals(entry.getValue().intValue(), count, "Unexpected listener count on " + bridge);
        }
    }

    @Test void bridgesLiveInOwningApplicationEventHandlersPackage() {
        var expected = Map.of(CompanyProfileIntegrationEventBridge.class, "profiles",
            RentalIntegrationEventBridge.class, "rentals", MaintenanceIntegrationEventBridge.class, "maintenance");
        for (var entry : expected.entrySet()) {
            var bridge = entry.getKey();
            assertEquals("com.maquigest.platform." + entry.getValue() + ".application.internal.eventhandlers",
                bridge.getPackageName());
            assertTrue(bridge.isAnnotationPresent(Component.class), "Bridge must be discovered by Spring");
            for (Method method : bridge.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(TransactionalEventListener.class)) continue;
                assertEquals(1, method.getParameterCount());
                assertEquals("com.maquigest.platform." + entry.getValue() + ".domain.model.events",
                    method.getParameterTypes()[0].getPackageName());
            }
        }
    }

    @Test void profileBridgePreservesBothPublishedContracts() {
        var publisher = mock(ApplicationEventPublisher.class);
        var bridge = new CompanyProfileIntegrationEventBridge(publisher);
        bridge.created(new CompanyProfileCreatedEvent(11L, 21L));
        bridge.updated(new CompanyProfileUpdatedEvent(12L, 22L));
        var captor = ArgumentCaptor.forClass(CompanyProfileIntegrationEvent.class);
        verify(publisher, times(2)).publishEvent(captor.capture());
        var events = captor.getAllValues();
        assertProfile(events.get(0), "company-profile.created", 11L, 21L);
        assertProfile(events.get(1), "company-profile.updated", 12L, 22L);
        verifyNoMoreInteractions(publisher);
    }

    @Test void rentalBridgePreservesAllThreePublishedContracts() {
        var publisher = mock(ApplicationEventPublisher.class);
        var bridge = new RentalIntegrationEventBridge(publisher);
        bridge.approved(new RentalRequestApprovedEvent(11L, 21L, 31L, 41L,
            LocalDate.of(2026, 11, 25), LocalDate.of(2026, 11, 30)));
        bridge.activated(new RentalActivatedEvent(12L, 22L));
        bridge.completed(new RentalCompletedEvent(13L, 23L));
        var captor = ArgumentCaptor.forClass(RentalIntegrationEvent.class);
        verify(publisher, times(3)).publishEvent(captor.capture());
        var events = captor.getAllValues();
        assertRental(events.get(0), "RENTAL_REQUEST_APPROVED", 11L, 21L);
        assertRental(events.get(1), "RENTAL_ACTIVATED", 12L, 22L);
        assertRental(events.get(2), "RENTAL_COMPLETED", 13L, 23L);
        verifyNoMoreInteractions(publisher);
    }

    @Test void maintenanceBridgePreservesAllFourPublishedContracts() {
        var publisher = mock(ApplicationEventPublisher.class);
        var bridge = new MaintenanceIntegrationEventBridge(publisher);
        bridge.reported(new IncidentReportedEvent(21L, true));
        bridge.resolved(new IncidentResolvedEvent(12L, 22L));
        bridge.created(new MaintenanceCreatedEvent(23L, "SCHEDULED"));
        bridge.updated(new MaintenanceStatusChangedEvent(14L, 24L, "COMPLETED"));
        var captor = ArgumentCaptor.forClass(MaintenanceIntegrationEvent.class);
        verify(publisher, times(4)).publishEvent(captor.capture());
        var events = captor.getAllValues();
        assertMaintenance(events.get(0), "INCIDENT_REPORTED", null, 21L);
        assertMaintenance(events.get(1), "INCIDENT_RESOLVED", 12L, 22L);
        assertMaintenance(events.get(2), "MAINTENANCE_CREATED", null, 23L);
        assertMaintenance(events.get(3), "MAINTENANCE_STATUS_CHANGED", 14L, 24L);
        verifyNoMoreInteractions(publisher);
    }

    private static void assertProfile(CompanyProfileIntegrationEvent event, String name, Long profileId, Long userId) {
        assertEquals(name, event.eventName());
        assertEquals(profileId, event.profileId());
        assertEquals(userId, event.userId());
        assertNotNull(event.occurredAt());
    }

    private static void assertRental(RentalIntegrationEvent event, String type, Long id, Long equipmentId) {
        assertEquals(type, event.eventType());
        assertEquals(id, event.aggregateId());
        assertEquals(equipmentId, event.equipmentId());
        assertNotNull(event.occurredAt());
    }

    private static void assertMaintenance(MaintenanceIntegrationEvent event, String type, Long id, Long equipmentId) {
        assertEquals(type, event.type());
        assertEquals(id, event.entityId());
        assertEquals(equipmentId, event.equipmentId());
        assertNotNull(event.occurredAt());
    }
}

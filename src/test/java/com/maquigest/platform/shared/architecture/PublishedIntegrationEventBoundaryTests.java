package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.profiles.infrastructure.events.CompanyProfileIntegrationEventBridge;
import com.maquigest.platform.rentals.infrastructure.events.RentalIntegrationEventBridge;
import com.maquigest.platform.maintenance.infrastructure.events.MaintenanceIntegrationEventBridge;
import com.maquigest.platform.profiles.interfaces.events.CompanyProfileIntegrationEvent;
import com.maquigest.platform.rentals.interfaces.events.RentalIntegrationEvent;
import com.maquigest.platform.maintenance.interfaces.events.MaintenanceIntegrationEvent;
import java.lang.reflect.Method;
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
        for (Class<?> bridge : new Class<?>[] {
                CompanyProfileIntegrationEventBridge.class,
                RentalIntegrationEventBridge.class,
                MaintenanceIntegrationEventBridge.class}) {
            int count = 0;
            for (Method method : bridge.getDeclaredMethods()) {
                if (!method.isAnnotationPresent(TransactionalEventListener.class)) continue;
                assertEquals(TransactionPhase.AFTER_COMMIT,
                    method.getAnnotation(TransactionalEventListener.class).phase(), method.toString());
                count++;
            }
            assertTrue(count > 0, "Missing @TransactionalEventListener on " + bridge);
        }
    }
}

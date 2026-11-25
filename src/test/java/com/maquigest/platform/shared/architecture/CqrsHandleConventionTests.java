package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.internal.queryservices.MaintenanceQueryServiceImpl;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.model.queries.GetMaintenanceActivitiesQuery;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.domain.repositories.*;
import com.maquigest.platform.rentals.application.internal.queryservices.RentalQueryServiceImpl;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.rentals.domain.model.queries.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import com.maquigest.platform.rentals.domain.repositories.*;
import com.maquigest.platform.subscriptions.application.internal.commandservices.*;
import com.maquigest.platform.subscriptions.application.ports.StripeCheckoutPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.domain.model.commands.ProcessSubscriptionWebhookCommand;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class CqrsHandleConventionTests {
    @Test void commandAndQueryServicesUseHandleWithContextLocalRecords() throws Exception {
        var root = Path.of("src", "main", "java");
        var contexts = root.resolve("com/maquigest/platform");
        try (var paths = Files.walk(contexts)) {
            var services = paths.filter(p -> p.getFileName().toString().matches(".*(?:Command|Query)Service\\.java"))
                .filter(p -> p.getParent().getFileName().toString().matches("commandservices|queryservices"))
                .toList();
            assertTrue(services.size() >= 33, "Missing CQRS service interfaces");
            for (var source : services) {
                var name = root.relativize(source).toString().replace('\\', '.').replace('/', '.').replaceFirst("\\.java$", "");
                var service = Class.forName(name);
                assertTrue(service.isInterface(), name);
                var context = name.substring(0, name.indexOf(".application."));
                var kind = name.endsWith("CommandService") ? "commands" : "queries";
                for (var method : service.getDeclaredMethods()) {
                    assertEquals("handle", method.getName(), method.toString());
                    assertTrue(method.getParameterCount() >= 1, method.toString());
                    var message = method.getParameterTypes()[0];
                    assertTrue(message.isRecord(), method.toString());
                    assertEquals(context + ".domain.model." + kind, message.getPackageName(), method.toString());
                }
            }
        }
    }

    @Test void maintenanceActivitiesKeepOwnerVisibility() {
        var maintenances = mock(MaintenanceRepository.class);
        var activities = mock(MaintenanceActivityRepository.class);
        var now = Instant.now();
        var maintenance = new Maintenance(7L, 2L, 3L, now, "Preventive", MaintenanceStatus.SCHEDULED, "", null);
        var activity = new MaintenanceActivity(1L, 7L, MaintenanceStatus.SCHEDULED, now, "", "Registered maintenance");
        when(maintenances.findById(7L)).thenReturn(Optional.of(maintenance));
        when(activities.findByMaintenanceId(7L)).thenReturn(List.of(activity));
        var service = new MaintenanceQueryServiceImpl(mock(IncidentRepository.class), maintenances, activities,
            mock(MaintenanceInventoryPort.class), mock(MaintenanceRentalPort.class));
        assertEquals(List.of(activity), service.handle(new GetMaintenanceActivitiesQuery(7L, 3L)));
        assertEquals("MAINTENANCE_NOT_FOUND", assertThrows(MaintenanceException.class,
            () -> service.handle(new GetMaintenanceActivitiesQuery(7L, 4L))).code());
        verify(activities, times(1)).findByMaintenanceId(7L);
    }

    @Test void rentalChildrenAreReadThroughVisibleRentalIds() {
        var rentals = mock(RentalRepository.class);
        var deliveries = mock(DeliveryRepository.class);
        var returns = mock(EquipmentReturnRepository.class);
        var now = Instant.now();
        var date = LocalDate.now();
        var rental = new Rental(7L, 2L, 3L, 4L, 5L, 6L, new RentalPeriod(date, date), RentalStatus.COMPLETED);
        var delivery = new Delivery(8L, 7L, now, "");
        var returned = new EquipmentReturn(9L, 7L, now, "", false);
        when(rentals.findVisibleTo(4L)).thenReturn(List.of(rental));
        when(rentals.findVisibleTo(99L)).thenReturn(List.of());
        when(deliveries.findByRentalIds(List.of(7L))).thenReturn(List.of(delivery));
        when(returns.findByRentalIds(List.of(7L))).thenReturn(List.of(returned));
        when(deliveries.findByRentalIds(List.of())).thenReturn(List.of());
        when(returns.findByRentalIds(List.of())).thenReturn(List.of());
        var service = new RentalQueryServiceImpl(mock(RentalRequestRepository.class), rentals, deliveries, returns);
        assertEquals(List.of(delivery), service.handle(new GetDeliveriesQuery(4L)));
        assertEquals(List.of(returned), service.handle(new GetEquipmentReturnsQuery(4L)));
        assertTrue(service.handle(new GetDeliveriesQuery(99L)).isEmpty());
        assertTrue(service.handle(new GetEquipmentReturnsQuery(99L)).isEmpty());
        verify(deliveries).findByRentalIds(List.of());
        verify(returns).findByRentalIds(List.of());
    }

    @Test void webhookReconcilesOnlyAfterVerificationWithOriginalBytes() {
        var stripe = mock(StripeCheckoutPort.class);
        var transactions = mock(SubscriptionCheckoutTransactions.class);
        var service = new SubscriptionCheckoutCommandServiceImpl(transactions, stripe);
        var checkout = SubscriptionCheckout.issue(3L, 1L, 7900, Instant.now().plusSeconds(7200));
        var session = new StripeCheckoutPort.Session("cs_test_a", checkout.getReference(), 7900,
            "pen", "complete", "paid", true, "payment", null);
        var payload = new byte[]{123, 10, -1, 125};
        when(stripe.isEnabled()).thenReturn(true);
        when(stripe.verifyWebhook(any(byte[].class), eq("sig"))).thenAnswer(invocation -> {
            assertArrayEquals(payload, invocation.getArgument(0, byte[].class));
            return Optional.of(session.id());
        });
        when(stripe.retrieve(session.id())).thenReturn(session);
        when(transactions.find(session.reference())).thenReturn(checkout);
        assertTrue(service.handle(new ProcessSubscriptionWebhookCommand(payload, "sig")).isSuccess());
        var order = inOrder(stripe, transactions);
        order.verify(stripe).isEnabled();
        order.verify(stripe).verifyWebhook(any(byte[].class), eq("sig"));
        order.verify(stripe).retrieve(session.id());
        order.verify(transactions).find(session.reference());
        order.verify(transactions).reconcile(3L, session);
    }

    @Test void webhookPayloadCannotBeMutatedThroughReferences() {
        var original = new byte[]{1, 2};
        var command = new ProcessSubscriptionWebhookCommand(original, "sig");
        original[0] = 9;
        command.payload()[1] = 9;
        assertArrayEquals(new byte[]{1, 2}, command.payload());
        assertEquals("sig", command.signature());
        assertNull(new ProcessSubscriptionWebhookCommand(null, null).payload());
    }
}

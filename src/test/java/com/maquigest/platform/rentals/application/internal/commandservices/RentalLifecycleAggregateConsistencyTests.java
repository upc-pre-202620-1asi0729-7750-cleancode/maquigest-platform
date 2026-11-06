package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.ports.RentalEquipmentPort;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.commands.RegisterDeliveryCommand;
import com.maquigest.platform.rentals.domain.model.commands.RegisterReturnCommand;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import com.maquigest.platform.rentals.domain.repositories.DeliveryRepository;
import com.maquigest.platform.rentals.domain.repositories.EquipmentReturnRepository;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Regression contract for Rental aggregate state and its delivery/return child entities. */
class RentalLifecycleAggregateConsistencyTests {
    private RentalRepository rentals;
    private DeliveryRepository deliveries;
    private EquipmentReturnRepository returns;
    private RentalEquipmentPort inventory;
    private RentalLifecycleTransactionalExecutor executor;
    private static final long RENTAL_ID = 10L;

    @BeforeEach void setup() {
        rentals = mock(RentalRepository.class);
        deliveries = mock(DeliveryRepository.class);
        returns = mock(EquipmentReturnRepository.class);
        inventory = mock(RentalEquipmentPort.class);
        executor = new RentalLifecycleTransactionalExecutor(rentals, deliveries, returns, inventory);
    }

    private Rental rental(RentalStatus status) {
        LocalDate today = LocalDate.now(ZoneId.of("America/Lima"));
        return new Rental(RENTAL_ID, 7L, 3L, 2L, 1L, 99L,
            new RentalPeriod(today.minusDays(2), today.plusDays(2)), status);
    }
    private static void conflict(Runnable action) {
        RentalsException exception = assertThrows(RentalsException.class, action::run);
        assertEquals("RENTALS_CONFLICT", exception.code());
    }
    private void noWrites() {
        verifyNoInteractions(inventory);
        verify(rentals, never()).save(any());
        verify(deliveries, never()).save(any());
        verify(returns, never()).save(any());
    }

    @Test void repeatedIdenticalDeliveryIsIdempotent() {
        Instant at = Instant.now().minusSeconds(60);
        Delivery recorded = new Delivery(20L, RENTAL_ID, at, "Handover");
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(deliveries.findByRentalId(RENTAL_ID)).thenReturn(Optional.of(recorded));
        Delivery actual = executor.handle(new RegisterDeliveryCommand(RENTAL_ID, 1L, at, " Handover "));
        assertSame(recorded, actual);
        noWrites();
    }

    @Test void changedSecondDeliveryConflictsBeforeAnyWrite() {
        Instant at = Instant.now().minusSeconds(60);
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(deliveries.findByRentalId(RENTAL_ID)).thenReturn(Optional.of(new Delivery(20L, RENTAL_ID, at, "First")));
        conflict(() -> executor.handle(new RegisterDeliveryCommand(RENTAL_ID, 1L, at, "Different")));
        noWrites();
    }

    @Test void repeatedIdenticalReturnIsIdempotent() {
        Instant at = Instant.now().minusSeconds(10);
        EquipmentReturn recorded = new EquipmentReturn(25L, RENTAL_ID, at, "Good", false);
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.COMPLETED)));
        when(returns.findByRentalId(RENTAL_ID)).thenReturn(Optional.of(recorded));
        EquipmentReturn actual = executor.handle(new RegisterReturnCommand(RENTAL_ID, 1L, at, " Good ", false));
        assertSame(recorded, actual);
        noWrites();
    }

    @Test void changedSecondReturnConflictsBeforeAnyWrite() {
        Instant at = Instant.now().minusSeconds(10);
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.COMPLETED)));
        when(returns.findByRentalId(RENTAL_ID)).thenReturn(Optional.of(new EquipmentReturn(25L, RENTAL_ID, at, "Good", false)));
        conflict(() -> executor.handle(new RegisterReturnCommand(RENTAL_ID, 1L, at, "Good", true)));
        noWrites();
    }

    @Test void returnRequiresRecordedDeliveryEvenWhenRentalStatusIsActive() {
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(returns.findByRentalId(RENTAL_ID)).thenReturn(Optional.empty());
        when(deliveries.findByRentalId(RENTAL_ID)).thenReturn(Optional.empty());
        conflict(() -> executor.handle(new RegisterReturnCommand(RENTAL_ID, 1L, Instant.now(), "", false)));
        noWrites();
    }

    @Test void returnCannotPrecedeDeliveryTimestamp() {
        Instant delivered = Instant.now().minusSeconds(30);
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(returns.findByRentalId(RENTAL_ID)).thenReturn(Optional.empty());
        when(deliveries.findByRentalId(RENTAL_ID)).thenReturn(Optional.of(new Delivery(20L, RENTAL_ID, delivered, "")));
        RentalsException failure = assertThrows(RentalsException.class,
            () -> executor.handle(new RegisterReturnCommand(RENTAL_ID, 1L, delivered.minusSeconds(1), "", false)));
        assertEquals("RENTALS_VALIDATION_ERROR", failure.code());
        noWrites();
    }

    @Test void confirmedRentalCannotReturnBeforeDeliveryStateTransition() {
        when(rentals.findByIdForUpdate(RENTAL_ID)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED)));
        when(returns.findByRentalId(RENTAL_ID)).thenReturn(Optional.empty());
        conflict(() -> executor.handle(new RegisterReturnCommand(RENTAL_ID, 1L, Instant.now(), "", false)));
        noWrites();
    }

    @Test void domainAggregateRejectsReentrancyAndReversals() {
        Rental rental = rental(RentalStatus.CONFIRMED);
        assertThrows(IllegalStateException.class, rental::complete);
        rental.deliver();
        assertThrows(IllegalStateException.class, rental::deliver);
        rental.complete();
        assertThrows(IllegalStateException.class, rental::complete);
        assertThrows(IllegalStateException.class, rental::deliver);
        assertEquals(RentalStatus.COMPLETED, rental.getStatus());
    }
}

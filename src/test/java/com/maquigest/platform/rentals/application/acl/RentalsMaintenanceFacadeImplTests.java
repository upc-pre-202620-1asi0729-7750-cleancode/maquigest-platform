package com.maquigest.platform.rentals.application.acl;

import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import com.maquigest.platform.rentals.domain.repositories.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalsMaintenanceFacadeImplTests {
    private final RentalRepository rentals = mock(RentalRepository.class);
    private final DeliveryRepository deliveries = mock(DeliveryRepository.class);
    private final EquipmentReturnRepository returns = mock(EquipmentReturnRepository.class);
    private final RentalsMaintenanceFacadeImpl facade = new RentalsMaintenanceFacadeImpl(rentals, deliveries, returns);
    private Rental rental(long id,long equipment,RentalStatus status) {
        return new Rental(id,7L,equipment,2L,1L,99L,
            new RentalPeriod(LocalDate.of(2026,11,25),LocalDate.of(2026,11,27)), status);
    }
    @Test void activeRentalChecksMatchingEquipment() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.ACTIVE)));
        assertTrue(facade.hasActiveRental(3L,1L));
        assertFalse(facade.hasActiveRental(4L,1L));
    }
    @Test void confirmedRentalIsNotActive() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.CONFIRMED)));
        assertFalse(facade.hasActiveRental(3L,1L));
    }
    @Test void completedRentalUsesActualReturnTimestamp() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.COMPLETED)));
        Instant actualReturn = Instant.parse("2026-11-04T12:00:00Z");
        when(returns.findByRentalId(10L)).thenReturn(Optional.of(new EquipmentReturn(1L,10L,actualReturn,"",false)));
        assertEquals(actualReturn,facade.history(3L,1L).getFirst().occurredAt());
    }
    @Test void completedRentalFallsBackToDelivery() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.COMPLETED)));
        Instant delivery = Instant.parse("2026-11-02T12:00:00Z");
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.of(new Delivery(1L,10L,delivery,"")));
        assertEquals(delivery, facade.history(3L,1L).getFirst().occurredAt());
    }
    @Test void activeRentalUsesDeliveryTimestamp() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.ACTIVE)));
        Instant delivery = Instant.parse("2026-11-02T12:00:00Z");
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.of(new Delivery(1L,10L,delivery,"")));
        assertEquals(delivery,facade.history(3L,1L).getFirst().occurredAt());
    }
    @Test void confirmedRentalUsesLimaLocalStartDate() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.CONFIRMED)));
        var item=facade.history(3L,1L).getFirst();
        assertEquals(LocalDate.of(2026,11,25).atStartOfDay(ZoneId.of("America/Lima")).toInstant(),item.occurredAt());
        assertEquals("Rental #10 (2026-11-25 to 2026-11-27)",item.description());
    }
    @Test void historyOnlyIncludesEquipmentOwnedInVisibleRentals() {
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rental(10L,3L,RentalStatus.CONFIRMED),rental(11L,5L,RentalStatus.ACTIVE)));
        assertEquals(1,facade.history(3L,1L).size());
    }
}

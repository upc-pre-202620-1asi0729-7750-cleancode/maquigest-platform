package com.maquigest.platform.rentals.application.internal.commandservices;
import com.maquigest.platform.rentals.application.ports.RentalEquipmentPort;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalLifecycleCommandServiceTests {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static Rental rental(RentalStatus status, LocalDate start, LocalDate end) {
        return new Rental(10L,7L,3L,2L,1L,99L,new RentalPeriod(start,end),status);
    }
    private static Rental rental(RentalStatus status) {
        var today=LocalDate.now(LIMA);
        return rental(status,today.minusDays(1),today.plusDays(2));
    }
    @Test void deliveryChangesInventoryAndRentalInOneUseCase() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED)));
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.empty());
        when(deliveries.save(any())).thenAnswer(a->a.getArgument(0));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        var result=svc.handle(new RegisterDeliveryCommand(10L,1L,Instant.now().minusSeconds(30),"On site"));
        assertEquals("On site",result.notes());
        verify(equipment).markDelivered(3L,1L,99L);
        verify(rentals).save(argThat(r->r.getStatus()==RentalStatus.ACTIVE));
    }
    @Test void returningReleasesOnlyOwnBlockAndMarksMaintenance() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.of(new Delivery(1L,10L,Instant.now().minusSeconds(7200),"")));
        when(returns.findByRentalId(10L)).thenReturn(Optional.empty());
        when(returns.save(any())).thenAnswer(a->a.getArgument(0));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        svc.handle(new RegisterReturnCommand(10L,1L,Instant.now().minusSeconds(3600),"Inspection",true));
        verify(equipment).completeReturn(3L,1L,99L,true);
        verify(rentals).save(argThat(r->r.getStatus()==RentalStatus.COMPLETED));
    }
    @Test void deliveryBeforeRentalPeriodIsRejectedWithoutInventoryChanges() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        var future=LocalDate.now(LIMA).plusDays(5);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED,future,future.plusDays(2))));
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.empty());
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        assertEquals(409,assertThrows(RentalsException.class,()->svc.handle(new RegisterDeliveryCommand(
            10L,1L,Instant.now(),"Too early"))).status().value());
        verifyNoInteractions(equipment);
        verify(rentals,never()).save(any());
    }
    @Test void deliveryWithFutureTimestampIsRejected() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED)));
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.empty());
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        assertEquals(400,assertThrows(RentalsException.class,()->svc.handle(new RegisterDeliveryCommand(
            10L,1L,Instant.now().plusSeconds(3600),"Future"))).status().value());
        verifyNoInteractions(equipment);
    }
    @Test void returnWithFutureTimestampIsRejected() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(returns.findByRentalId(10L)).thenReturn(Optional.empty());
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.of(new Delivery(1L,10L,Instant.now().minusSeconds(3600),"")));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        assertEquals(400,assertThrows(RentalsException.class,()->svc.handle(new RegisterReturnCommand(
            10L,1L,Instant.now().plusSeconds(3600),"Future",false))).status().value());
        verifyNoInteractions(equipment);
    }
    @Test void returnBeforeRentalPeriodStartsIsRejected() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        var future=LocalDate.now(LIMA).plusDays(5);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE,future,future.plusDays(2))));
        when(returns.findByRentalId(10L)).thenReturn(Optional.empty());
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.of(new Delivery(1L,10L,Instant.now().minusSeconds(3600),"")));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        assertEquals(409,assertThrows(RentalsException.class,()->svc.handle(new RegisterReturnCommand(
            10L,1L,Instant.now(),"Before period",false))).status().value());
        verifyNoInteractions(equipment);
    }
    @Test void unauthorizedDeliveryCannotMutateInventory() {
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED)));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        assertEquals(403,assertThrows(RentalsException.class,()->svc.handle(new RegisterDeliveryCommand(
            10L,2L,Instant.now(),""))).status().value());
        verifyNoInteractions(equipment);
    }
}

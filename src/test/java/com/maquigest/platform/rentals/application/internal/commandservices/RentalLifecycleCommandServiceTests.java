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
    private static Rental rental(RentalStatus status){return new Rental(10L,7L,3L,2L,1L,99L,
        new RentalPeriod(LocalDate.of(2026,12,1),LocalDate.of(2026,12,3)),status);}
    @Test void deliveryChangesInventoryAndRentalInOneUseCase(){
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED)));
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.empty());
        when(deliveries.save(any())).thenAnswer(a->a.getArgument(0));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        var result=svc.handle(new RegisterDeliveryCommand(10L,1L,Instant.parse("2026-12-01T12:00:00Z"),"On site"));
        assertEquals("On site",result.notes());
        verify(equipment).markDelivered(3L,1L,99L);
        verify(rentals).save(argThat(r->r.getStatus()==RentalStatus.ACTIVE));
    }
    @Test void returningReleasesOnlyOwnBlockAndMarksMaintenance(){
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.ACTIVE)));
        when(deliveries.findByRentalId(10L)).thenReturn(Optional.of(new Delivery(1L,10L,
            Instant.parse("2026-12-01T12:00:00Z"),"")));
        when(returns.findByRentalId(10L)).thenReturn(Optional.empty());
        when(returns.save(any())).thenAnswer(a->a.getArgument(0));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        svc.handle(new RegisterReturnCommand(10L,1L,Instant.parse("2026-12-03T12:00:00Z"),"Inspection",true));
        verify(equipment).completeReturn(3L,1L,99L,true);
        verify(rentals).save(argThat(r->r.getStatus()==RentalStatus.COMPLETED));
    }
    @Test void unauthorizedDeliveryCannotMutateInventory(){
        var rentals=mock(RentalRepository.class);var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);var equipment=mock(RentalEquipmentPort.class);
        when(rentals.findByIdForUpdate(10L)).thenReturn(Optional.of(rental(RentalStatus.CONFIRMED)));
        var svc=new RentalLifecycleCommandServiceImpl(rentals,deliveries,returns,equipment);
        assertEquals(403,assertThrows(RentalsException.class,()->svc.handle(new RegisterDeliveryCommand(
            10L,2L,Instant.now(),""))).status().value());
        verifyNoInteractions(equipment);
    }
}

package com.maquigest.platform.rentals.application.internal.commandservices;
import com.maquigest.platform.rentals.application.ports.*;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.aggregates.*;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalRequestCommandServiceTests {
    private static RentalPeriod period(){return new RentalPeriod(LocalDate.of(2026,12,1),LocalDate.of(2026,12,3));}
    private static RentalRequest pending(){return new RentalRequest(7L,3L,2L,1L,period(),RentalRequestStatus.PENDING,Instant.now());}
    @Test void approvalReservesInventoryAndCreatesExactlyOneRental(){
        var requests=mock(RentalRequestRepository.class);var rentals=mock(RentalRepository.class);
        var inventory=mock(RentalEquipmentPort.class);var subscriptions=mock(RentalSubscriptionPort.class);
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(pending()));
        when(subscriptions.canManageRentals(1L)).thenReturn(true);
        when(inventory.reserve(3L,1L,period())).thenReturn(99L);
        when(rentals.findByRequestId(7L)).thenReturn(Optional.empty());
        when(requests.save(any())).thenAnswer(a->a.getArgument(0));
        var service=new RentalRequestTransactionalExecutor(requests,rentals,inventory,subscriptions);
        var result=service.handle(new ResolveRentalRequestCommand(7L,1L,RentalRequestStatus.APPROVED));
        assertEquals(RentalRequestStatus.APPROVED,result.getStatus());
        verify(rentals,times(1)).save(any());verify(inventory).reserve(3L,1L,period());
    }
    @Test void anotherOwnerCannotApproveOrReserve(){
        var requests=mock(RentalRequestRepository.class);var rentals=mock(RentalRepository.class);
        var inventory=mock(RentalEquipmentPort.class);var subscriptions=mock(RentalSubscriptionPort.class);
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(pending()));
        var service=new RentalRequestTransactionalExecutor(requests,rentals,inventory,subscriptions);
        assertEquals(403,com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(assertThrows(RentalsException.class, () -> service.handle(new ResolveRentalRequestCommand(7L,22L,RentalRequestStatus.APPROVED))).code()).value());
        verifyNoInteractions(inventory);
    }
    @Test void rejectingNeverReservesInventory(){
        var requests=mock(RentalRequestRepository.class);var rentals=mock(RentalRepository.class);
        var inventory=mock(RentalEquipmentPort.class);var subscriptions=mock(RentalSubscriptionPort.class);
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(pending()));
        when(requests.save(any())).thenAnswer(a->a.getArgument(0));
        var result=new RentalRequestTransactionalExecutor(requests,rentals,inventory,subscriptions)
            .handle(new ResolveRentalRequestCommand(7L,1L,RentalRequestStatus.REJECTED));
        assertEquals(RentalRequestStatus.REJECTED,result.getStatus());verifyNoInteractions(inventory,rentals);
    }
}

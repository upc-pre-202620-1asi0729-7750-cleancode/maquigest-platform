package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.ports.RentalEquipmentPort;
import com.maquigest.platform.rentals.application.ports.RentalSubscriptionPort;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.commands.CreateRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.commands.RescheduleRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import com.maquigest.platform.rentals.domain.repositories.RentalRequestRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RentalRequestReschedulingTests {
    private final RentalRequestRepository requests = mock(RentalRequestRepository.class);
    private final RentalRepository rentals = mock(RentalRepository.class);
    private final RentalEquipmentPort equipment = mock(RentalEquipmentPort.class);
    private final RentalSubscriptionPort subscriptions = mock(RentalSubscriptionPort.class);
    private final RentalRequestCommandServiceImpl service =
        new RentalRequestCommandServiceImpl(requests, rentals, equipment, subscriptions);

    private static RentalPeriod period(int shift) {
        LocalDate start = LocalDate.now(ZoneId.of("America/Lima")).plusDays(10 + shift);
        return new RentalPeriod(start, start.plusDays(2));
    }

    private static RentalRequest request(RentalRequestStatus status) {
        return new RentalRequest(7L, 3L, 2L, 1L, period(0), status, Instant.now());
    }

    @Test void creationAcquiresPairLockBeforeCheckingForOverlap() {
        when(requests.existsOverlappingActiveRequest(3L, 2L, period(0), null)).thenReturn(true);
        assertThrows(RentalsException.class, () ->
            service.handle(new CreateRentalRequestCommand(3L, 2L, 1L, period(0))));
        var order = inOrder(requests);
        order.verify(requests).lockRequestSubmission(3L, 2L);
        order.verify(requests).existsOverlappingActiveRequest(3L, 2L, period(0), null);
        verifyNoInteractions(equipment);
    }

    @Test void reschedulingAcquiresSamePairLockBeforeCheckingForOverlap() {
        when(requests.findByIdForUpdate(7L))
            .thenReturn(Optional.of(request(RentalRequestStatus.PENDING)));
        when(requests.existsOverlappingActiveRequest(3L, 2L, period(4), 7L)).thenReturn(true);
        assertThrows(RentalsException.class, () ->
            service.handle(new RescheduleRentalRequestCommand(7L, 2L, period(4))));
        var order = inOrder(requests);
        order.verify(requests).findByIdForUpdate(7L);
        order.verify(requests).lockRequestSubmission(3L, 2L);
        order.verify(requests).existsOverlappingActiveRequest(3L, 2L, period(4), 7L);
        verifyNoInteractions(equipment);
    }

    @Test void malformedEquipmentIsRejectedWithoutAcquiringLock() {
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new CreateRentalRequestCommand(null, 2L, 1L, period(0))));
        assertEquals(400, ex.status().value());
        verifyNoInteractions(requests, equipment, subscriptions);
    }

    @Test void requesterCannotSubmitOwnEquipment() {
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new CreateRentalRequestCommand(3L, 2L, 2L, period(0))));
        assertEquals(400, ex.status().value());
        verifyNoInteractions(requests, equipment, subscriptions);
    }

    @Test void anotherRequesterCannotLockOrModifyPendingRequest() {
        when(requests.findByIdForUpdate(7L))
            .thenReturn(Optional.of(request(RentalRequestStatus.PENDING)));
        assertThrows(RentalsException.class, () ->
            service.handle(new RescheduleRentalRequestCommand(7L, 44L, period(4))));
        verify(requests, never()).lockRequestSubmission(any(), any());
    }

    @Test void noOpRescheduleDoesNotDuplicateOrUpdateRows() {
        var current = request(RentalRequestStatus.PENDING);
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(current));
        var returned = service.handle(new RescheduleRentalRequestCommand(7L, 2L, period(0)));
        assertSame(current, returned);
        verify(requests).lockRequestSubmission(3L, 2L);
        verify(requests, never()).save(any());
        verify(requests, never()).existsOverlappingActiveRequest(any(), any(), any(), any());
    }

    @Test void duplicatePendingRequestIsRejectedBeforeSaving() {
        when(requests.existsOverlappingActiveRequest(3L, 2L, period(0), null)).thenReturn(true);
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new CreateRentalRequestCommand(3L, 2L, 1L, period(0))));
        assertEquals(409, ex.status().value());
        verify(requests, never()).save(any());
        verifyNoInteractions(equipment);
    }

    @Test void differentRequesterCanSubmitWithAvailableEquipment() {
        when(equipment.inspect(3L, period(0)))
            .thenReturn(new RentalEquipmentPort.EquipmentFacts(3L, 1L, true));
        when(subscriptions.hasActiveSubscription(1L)).thenReturn(true);
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var created = service.handle(new CreateRentalRequestCommand(3L, 44L, 1L, period(0)));
        assertEquals(44L, created.getConstructionUserId());
        assertEquals(RentalRequestStatus.PENDING, created.getStatus());
        verify(requests).existsOverlappingActiveRequest(3L, 44L, period(0), null);
        verify(requests).save(any());
    }

    @Test void pendingOwnerCanChangeDatesWithoutChangingId() {
        var current = request(RentalRequestStatus.PENDING);
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(current));
        when(equipment.inspect(3L, period(4)))
            .thenReturn(new RentalEquipmentPort.EquipmentFacts(3L, 1L, true));
        when(subscriptions.hasActiveSubscription(1L)).thenReturn(true);
        when(requests.save(any())).thenAnswer(inv -> inv.getArgument(0));
        var updated = service.handle(new RescheduleRentalRequestCommand(7L, 2L, period(4)));
        assertEquals(7L, updated.getId());
        assertEquals(period(4), updated.getPeriod());
        assertEquals(RentalRequestStatus.PENDING, updated.getStatus());
        verify(requests).existsOverlappingActiveRequest(3L, 2L, period(4), 7L);
        verify(requests).save(any());
        verifyNoInteractions(rentals);
    }

    @Test void anotherRequesterCannotChangeDates() {
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(request(RentalRequestStatus.PENDING)));
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new RescheduleRentalRequestCommand(7L, 44L, period(4))));
        assertEquals(403, ex.status().value());
        verify(requests, never()).save(any());
    }

    @Test void approvedRequestCannotChangeDates() {
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(request(RentalRequestStatus.APPROVED)));
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new RescheduleRentalRequestCommand(7L, 2L, period(4))));
        assertEquals(409, ex.status().value());
        verify(requests, never()).save(any());
    }

    @Test void editingOntoAnotherPendingRequestIsRejected() {
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(request(RentalRequestStatus.PENDING)));
        when(requests.existsOverlappingActiveRequest(3L, 2L, period(4), 7L)).thenReturn(true);
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new RescheduleRentalRequestCommand(7L, 2L, period(4))));
        assertEquals(409, ex.status().value());
        verify(requests, never()).save(any());
    }

    @Test void editingOntoUnavailableEquipmentIsRejected() {
        when(requests.findByIdForUpdate(7L)).thenReturn(Optional.of(request(RentalRequestStatus.PENDING)));
        when(equipment.inspect(3L, period(4)))
            .thenReturn(new RentalEquipmentPort.EquipmentFacts(3L, 1L, false));
        var ex = assertThrows(RentalsException.class, () ->
            service.handle(new RescheduleRentalRequestCommand(7L, 2L, period(4))));
        assertEquals(409, ex.status().value());
        verify(requests, never()).save(any());
    }
}

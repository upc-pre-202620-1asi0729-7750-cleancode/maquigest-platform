package com.maquigest.platform.rentals.interfaces.rest;

import com.maquigest.platform.rentals.application.commandservices.RentalRequestCommandService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalRequestByIdQuery;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.interfaces.rest.resources.*;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalRequestResourceBoundaryTests {
    private static final Instant START = Instant.parse("2026-11-16T05:00:00Z");
    private static final Instant END = Instant.parse("2026-11-19T04:59:59.999Z");
    private static final Instant CREATED = Instant.parse("2026-11-01T12:00:00Z");
    private static final Instant CLIENT_CREATED = Instant.parse("2000-01-01T00:00:00Z");
    private static final RentalPeriod PERIOD = new RentalPeriod(LocalDate.of(2026, 11, 16), LocalDate.of(2026, 11, 18));
    private final RentalRequestCommandService commands = mock(RentalRequestCommandService.class);
    private final RentalQueryService queries = mock(RentalQueryService.class);
    private final RentalActorPolicy policy = mock(RentalActorPolicy.class);
    private final Authentication auth = mock(Authentication.class);
    private final RentalRequestsController controller = new RentalRequestsController(commands, queries, policy);

    private void customer() {
        when(policy.current(auth)).thenReturn(Optional.of(new RentalActorPolicy.Actor(20L, true, false)));
    }

    private void owner() {
        when(policy.current(auth)).thenReturn(Optional.of(new RentalActorPolicy.Actor(30L, false, true)));
        when(queries.handle(new GetRentalRequestByIdQuery(10L, 30L))).thenReturn(Optional.of(request(RentalRequestStatus.PENDING)));
    }

    private RentalRequest request(RentalRequestStatus status) {
        return new RentalRequest(10L, 40L, 20L, 30L, PERIOD, status, CREATED);
    }

    private RentalRequestResource response(RentalRequestStatus status) {
        return new RentalRequestResource(10L, 40L, 20L, 30L, START, END, status, CREATED);
    }

    @Test void createMapsAuthenticatedActorAndIgnoresLegacyIdAndCreatedAt() {
        customer();
        var expected = new CreateRentalRequestCommand(40L, 20L, 30L, PERIOD);
        when(commands.handle(expected)).thenReturn(Result.success(request(RentalRequestStatus.PENDING)));
        var result = controller.create(new CreateRentalRequestResource(999L, 40L, 20L, 30L, START, END,
            RentalRequestStatus.PENDING, CLIENT_CREATED), auth);
        assertEquals(201, result.getStatusCode().value());
        assertEquals(response(RentalRequestStatus.PENDING), result.getBody());
        verify(commands).handle(expected);
    }

    @Test void createStillAcceptsOmittedOptionalFields() {
        customer();
        var expected = new CreateRentalRequestCommand(40L, 20L, 30L, PERIOD);
        when(commands.handle(expected)).thenReturn(Result.success(request(RentalRequestStatus.PENDING)));
        var result = controller.create(new CreateRentalRequestResource(null, 40L, 20L, 30L, START, END, null, null), auth);
        assertEquals(201, result.getStatusCode().value());
        assertEquals(response(RentalRequestStatus.PENDING), result.getBody());
        verify(commands).handle(expected);
    }

    @Test void createRejectsForgedActorBeforeMapping() {
        customer();
        var error = assertThrows(RentalsException.class, () -> controller.create(
            new CreateRentalRequestResource(null, 40L, 99L, 30L, null, null, RentalRequestStatus.APPROVED, null), auth));
        assertEquals("RENTALS_FORBIDDEN", error.code());
        verifyNoInteractions(commands, queries);
    }

    @Test void createRejectsNonPendingStatusBeforeMapping() {
        customer();
        var error = assertThrows(RentalsException.class, () -> controller.create(
            new CreateRentalRequestResource(null, 40L, 20L, 30L, null, null, RentalRequestStatus.APPROVED, null), auth));
        assertEquals("New requests must be PENDING", error.getMessage());
        verifyNoInteractions(commands, queries);
    }

    @Test void ownerDecisionMapsPathAndActorAndKeepsResponseContract() {
        owner();
        var expected = new ResolveRentalRequestCommand(10L, 30L, RentalRequestStatus.APPROVED);
        when(commands.handle(expected)).thenReturn(Result.success(request(RentalRequestStatus.APPROVED)));
        var result = controller.update(10L, new UpdateRentalRequestResource(10L, 40L, 20L, 30L, START, END,
            RentalRequestStatus.APPROVED, CLIENT_CREATED), auth);
        assertEquals(response(RentalRequestStatus.APPROVED), result);
        verify(commands).handle(expected);
    }

    @Test void ownerDecisionStillRejectsChangesToIdentityAndPeriod() {
        owner();
        var forgedEquipment = new UpdateRentalRequestResource(10L, 41L, 20L, 30L, START, END, RentalRequestStatus.APPROVED, null);
        var forgedPeriod = new UpdateRentalRequestResource(10L, 40L, 20L, 30L, START, END.plusSeconds(1), RentalRequestStatus.APPROVED, null);
        for (var payload : new UpdateRentalRequestResource[]{forgedEquipment, forgedPeriod}) {
            var error = assertThrows(RentalsException.class, () -> controller.update(10L, payload, auth));
            assertEquals("Request participants, equipment and dates cannot be edited", error.getMessage());
        }
        verifyNoInteractions(commands);
    }

    @Test void rescheduleMapsInclusiveLimaPeriodAndKeepsResponseContract() {
        customer();
        var expected = new RescheduleRentalRequestCommand(10L, 20L, PERIOD);
        when(commands.handle(expected)).thenReturn(Result.success(request(RentalRequestStatus.PENDING)));
        assertEquals(response(RentalRequestStatus.PENDING), controller.reschedule(10L, new RescheduleRentalRequestResource(START, END), auth));
        verify(commands).handle(expected);
    }
}

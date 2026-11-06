package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.internal.commandservices.*;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.internal.commandservices.*;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandResultBoundaryTests {
    private static void failure(Result<?, ApplicationError> result, String code, int status) {
        var typed = assertInstanceOf(Result.Failure.class, result);
        var error = assertInstanceOf(ApplicationError.class, typed.error());
        assertEquals(code, error.code());
        assertEquals(status, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(error.code()).value());
    }

    @Test void expectedFailuresAreResultsOutsideTransactionalExecutors() {
        var rentalRequest = mock(RentalRequestTransactionalExecutor.class);
        when(rentalRequest.handle((CreateRentalRequestCommand) null)).thenThrow(RentalsException.forbidden());
        failure(new RentalRequestCommandServiceImpl(rentalRequest).handle((CreateRentalRequestCommand) null), "RENTALS_FORBIDDEN", 403);

        var lifecycle = mock(RentalLifecycleTransactionalExecutor.class);
        when(lifecycle.handle((RegisterDeliveryCommand) null)).thenThrow(RentalsException.invalid("bad delivery"));
        failure(new RentalLifecycleCommandServiceImpl(lifecycle).handle((RegisterDeliveryCommand) null), "RENTALS_VALIDATION_ERROR", 400);

        var maintenance = mock(MaintenanceTransactionalExecutor.class);
        when(maintenance.handle((RegisterMaintenanceCommand) null, 3L)).thenThrow(MaintenanceException.conflict("busy"));
        failure(new MaintenanceCommandServiceImpl(maintenance).handle((RegisterMaintenanceCommand) null, 3L), "MAINTENANCE_CONFLICT", 409);

        var incident = mock(IncidentTransactionalExecutor.class);
        when(incident.handle((ReportIncidentCommand) null, 3L)).thenThrow(MaintenanceException.forbidden());
        failure(new IncidentCommandServiceImpl(incident).handle((ReportIncidentCommand) null, 3L), "MAINTENANCE_FORBIDDEN", 403);
    }

    @Test void unexpectedRuntimeFailureIsNotSwallowedByResult() {
        var executor = mock(RentalRequestTransactionalExecutor.class);
        when(executor.handle((CreateRentalRequestCommand) null)).thenThrow(new IllegalStateException("database failed"));
        var wrapper = new RentalRequestCommandServiceImpl(executor);
        assertThrows(IllegalStateException.class, () -> wrapper.handle((CreateRentalRequestCommand) null));
    }

    @Test void transactionBoundariesStayOnExecutorsNotOnResultWrappers() throws Exception {
        for (Class<?> wrapper : List.of(RentalRequestCommandServiceImpl.class,
                RentalLifecycleCommandServiceImpl.class, MaintenanceCommandServiceImpl.class,
                IncidentCommandServiceImpl.class)) {
            assertFalse(wrapper.isAnnotationPresent(Transactional.class), wrapper.getName());
        }
        for (Class<?> executor : List.of(RentalRequestTransactionalExecutor.class,
                RentalLifecycleTransactionalExecutor.class, MaintenanceTransactionalExecutor.class,
                IncidentTransactionalExecutor.class)) {
            boolean transactional = executor.isAnnotationPresent(Transactional.class)
                    || java.util.Arrays.stream(executor.getDeclaredMethods())
                        .anyMatch(method -> method.isAnnotationPresent(Transactional.class));
            assertTrue(transactional, executor.getName());
        }
    }

    @Test void restBoundaryConvertsFailuresAfterExecutorAndKeepsOriginalErrorCode() {
        var result = Result.<String, ApplicationError>failure(new ApplicationError("RENTALS_NOT_FOUND", "Rental not found"));
        var ex = assertThrows(RentalsException.class, () ->
            ResponseEntityAssembler.valueOrThrow(result, RentalsException::fromApplicationError));
        assertEquals("RENTALS_NOT_FOUND", ex.code());
        assertEquals("Rental not found", ex.getMessage());
        assertEquals(404, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(ex.code()).value());
    }
}

package com.maquigest.platform.shared.interfaces.rest.transform;

import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.interfaces.rest.MaintenanceExceptionHandler;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.interfaces.rest.RentalsExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandResultHttpMappingTests {
    @Test void everyExpectedRentalsStatusIsUnchanged() {
        var handler = new RentalsExceptionHandler();
        for (var test : List.of(
                RentalsException.invalid("invalid"), RentalsException.forbidden(),
                RentalsException.notFound("Rental"), RentalsException.conflict("conflict"))) {
            int expected = switch (test.code()) {
                case "RENTALS_VALIDATION_ERROR" -> 400;
                case "RENTALS_FORBIDDEN" -> 403;
                case "RENTALS_NOT_FOUND" -> 404;
                case "RENTALS_CONFLICT" -> 409;
                default -> throw new AssertionError(test.code());
            };
            var response = handler.handle(test);
            assertEquals(expected, response.getStatusCode().value());
            assertEquals(test.code(), response.getBody().getProperties().get("code"));
        }
    }
    @Test void everyExpectedMaintenanceStatusIsUnchanged() {
        var handler = new MaintenanceExceptionHandler();
        for (var test : List.of(
                MaintenanceException.invalid("invalid"), MaintenanceException.forbidden(),
                MaintenanceException.notFound("Maintenance"), MaintenanceException.conflict("conflict"))) {
            int expected = switch (test.code()) {
                case "MAINTENANCE_INVALID" -> 400;
                case "MAINTENANCE_FORBIDDEN" -> 403;
                case "MAINTENANCE_NOT_FOUND" -> 404;
                case "MAINTENANCE_CONFLICT" -> 409;
                default -> throw new AssertionError(test.code());
            };
            var response = handler.handle(test);
            assertEquals(expected, response.getStatusCode().value());
            assertEquals(test.code(), response.getBody().getProperties().get("code"));
        }
    }
    @Test void globalErrorCodeAssemblerAlsoHandlesForbiddenAndUnauthorized() {
        assertEquals(403, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("RENTALS_FORBIDDEN").value());
        assertEquals(403, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("MAINTENANCE_FORBIDDEN").value());
        assertEquals(401, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("IAM_UNAUTHORIZED").value());
        assertEquals(422, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("BUSINESS_RULE_VIOLATION").value());
    }
}

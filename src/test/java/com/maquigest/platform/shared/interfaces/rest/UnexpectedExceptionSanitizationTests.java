package com.maquigest.platform.shared.interfaces.rest;

import com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;
import static org.junit.jupiter.api.Assertions.*;

/** Avoid leaking SQL, secrets or internal exception messages in HTTP 500 bodies. */
class UnexpectedExceptionSanitizationTests {
    private static final String SECRET = "jdbc:mysql://internal-db?password=do-not-disclose";

    @Test void runtimeExceptionReturnsGenericProblemDetail() {
        ProblemDetail body = new GlobalExceptionHandler()
            .handleRuntimeException(new IllegalStateException(SECRET)).getBody();
        assertSanitized(body);
    }

    @Test void checkedExceptionReturnsGenericProblemDetail() {
        ProblemDetail body = new GlobalExceptionHandler()
            .handleException(new Exception(SECRET)).getBody();
        assertSanitized(body);
    }

    @Test void errorMappingKeepsOriginalHttpStatuses() {
        assertEquals(400, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("VALIDATION_ERROR").value());
        assertEquals(401, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("IAM_UNAUTHORIZED").value());
        assertEquals(403, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("RENTALS_FORBIDDEN").value());
        assertEquals(404, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("PROFILE_NOT_FOUND").value());
        assertEquals(409, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("RENTALS_CONFLICT").value());
        assertEquals(422, ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode("BUSINESS_RULE_VIOLATION").value());
    }

    private void assertSanitized(ProblemDetail body) {
        assertNotNull(body);
        assertEquals(500, body.getStatus());
        assertEquals("UNEXPECTED_ERROR", body.getProperties().get("code"));
        assertFalse(body.toString().contains(SECRET));
        assertFalse(body.toString().contains("password="));
    }
}

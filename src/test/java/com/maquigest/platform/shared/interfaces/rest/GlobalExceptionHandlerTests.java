package com.maquigest.platform.shared.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTests {

    @Test
    void shouldReturnBadRequestForInvalidJsonEnumWithoutExposingJacksonDetails() {
        var request = new MockHttpInputMessage(
                "{\"status\":\"INVALID_STATE\"}".getBytes(StandardCharsets.UTF_8)
        );
        var exception = new HttpMessageNotReadableException(
                "Cannot deserialize value of EquipmentStatus from INVALID_STATE", request
        );

        var response = new GlobalExceptionHandler().handleHttpMessageNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        var problem = response.getBody();
        assertNotNull(problem);
        assertEquals(400, problem.getStatus());
        assertNotNull(problem.getProperties());
        assertEquals("VALIDATION_ERROR", problem.getProperties().get("code"));
        assertFalse(problem.toString().contains("Cannot deserialize value"));
    }
}

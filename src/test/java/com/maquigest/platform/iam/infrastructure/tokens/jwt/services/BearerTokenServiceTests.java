package com.maquigest.platform.iam.infrastructure.tokens.jwt.services;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

class BearerTokenServiceTests {
    private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void extractsBearerTokenFromAuthorizationHeader() {
        var service = new TokenServiceImpl(TEST_SECRET, 7);
        var request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        assertEquals("valid-token", service.getBearerTokenFrom(request));
    }

    @Test
    void ignoresMissingOrWrongScheme() {
        var service = new TokenServiceImpl(TEST_SECRET, 7);
        var request = new MockHttpServletRequest();
        assertNull(service.getBearerTokenFrom(request));
        request.addHeader("Authorization", "Basic encoded");
        assertNull(service.getBearerTokenFrom(request));
    }

    @Test
    void generatesJwtFromAuthenticatedPrincipal() {
        var service = new TokenServiceImpl(TEST_SECRET, 7);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "ada@example.com", null, java.util.List.of());
        var token = service.generateToken(authentication);
        assertTrue(service.validateToken(token));
        assertEquals("ada@example.com", service.getUsernameFromToken(token));
    }
}

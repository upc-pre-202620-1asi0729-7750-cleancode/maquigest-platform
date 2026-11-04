package com.maquigest.platform.iam.infrastructure.tokens.jwt.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceImplTests {
    private static final String TEST_SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void signsAndValidatesJwtForRegisteredEmail() {
        var service = new TokenServiceImpl(TEST_SECRET, 7);
        var token = service.generateToken("ada@example.com");
        assertTrue(service.validateToken(token));
        assertEquals("ada@example.com", service.getUsernameFromToken(token));
    }

    @Test
    void rejectsUnsignedOrForgedTokens() {
        var service = new TokenServiceImpl(TEST_SECRET, 7);
        assertFalse(service.validateToken("not-a-jwt"));
        var otherService = new TokenServiceImpl("another-0123456789abcdef0123456789abcdef0123456789abcdef", 7);
        assertFalse(otherService.validateToken(service.generateToken("ada@example.com")));
    }

    @Test
    void rejectsWeakSecrets() {
        assertThrows(IllegalArgumentException.class, () -> new TokenServiceImpl("weak", 7));
    }
}

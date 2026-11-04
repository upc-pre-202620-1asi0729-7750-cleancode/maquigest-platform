package com.maquigest.platform.iam.infrastructure.authorization.sfs.pipeline;

import com.maquigest.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.maquigest.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BearerAuthorizationRequestFilterTests {
    private final BearerTokenService tokens = mock(BearerTokenService.class);
    private final UserDetailsService details = mock(UserDetailsService.class);
    private final BearerAuthorizationRequestFilter filter =
            new BearerAuthorizationRequestFilter(tokens, details);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validJwtCreatesAuthenticatedPrincipal() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/authentication/me");
        request.addHeader("Authorization", "Bearer jwt-token");
        when(tokens.getBearerTokenFrom(request)).thenReturn("jwt-token");
        when(tokens.validateToken("jwt-token")).thenReturn(true);
        when(tokens.getUsernameFromToken("jwt-token")).thenReturn("ada@example.com");
        when(details.loadUserByUsername("ada@example.com")).thenReturn(new UserDetailsImpl(
                "ada@example.com", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_RENTAL_COMPANY")), true));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("ada@example.com", authentication.getName());
        assertEquals("ROLE_RENTAL_COMPANY", authentication.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void invalidJwtLeavesRequestUnauthenticated() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/authentication/me");
        when(tokens.getBearerTokenFrom(request)).thenReturn("invalid");
        when(tokens.validateToken("invalid")).thenReturn(false);

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(details);
    }

    @Test
    void tokenForDeletedOrInactiveUserIsRejected() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/authentication/me");
        when(tokens.getBearerTokenFrom(request)).thenReturn("old-token");
        when(tokens.validateToken("old-token")).thenReturn(true);
        when(tokens.getUsernameFromToken("old-token")).thenReturn("ada@example.com");
        when(details.loadUserByUsername("ada@example.com"))
                .thenThrow(new UsernameNotFoundException("Account not active"));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

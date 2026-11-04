package com.maquigest.platform.iam.infrastructure.authorization.sfs.pipeline;

import com.maquigest.platform.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.maquigest.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Validates bearer JWTs and restores a Spring Security principal on each request. */
@Slf4j
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {
    private final BearerTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService,
                                            UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            var token = tokenService.getBearerTokenFrom(request);
            if (token != null && tokenService.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                var email = tokenService.getUsernameFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(email);
                if (userDetails.isEnabled() && userDetails.isAccountNonLocked()
                        && userDetails.isAccountNonExpired()
                        && userDetails.isCredentialsNonExpired()) {
                    var authentication = UsernamePasswordAuthenticationTokenBuilder.build(userDetails, request);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // Never echo bearer tokens or secret material in logs.
            log.debug("Bearer authentication rejected: {}", ex.getClass().getSimpleName());
        }
        filterChain.doFilter(request, response);
    }
}

package com.maquigest.platform.iam.infrastructure.tokens.jwt;

import com.maquigest.platform.iam.application.internal.outboundservices.tokens.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/** Adds HTTP bearer-token extraction and Security Authentication support to the JWT port. */
public interface BearerTokenService extends TokenService {
    String getBearerTokenFrom(HttpServletRequest request);

    String generateToken(Authentication authentication);
}

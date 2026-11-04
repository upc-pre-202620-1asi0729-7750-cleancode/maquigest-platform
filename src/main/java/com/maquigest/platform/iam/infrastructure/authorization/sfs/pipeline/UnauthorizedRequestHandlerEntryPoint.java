
package com.maquigest.platform.iam.infrastructure.authorization.sfs.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Maps unauthenticated access to HTTP 401.
 */
@Component
@Slf4j
public class UnauthorizedRequestHandlerEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        log.debug("Unauthenticated request to {}", request.getRequestURI());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"type\":\"about:blank\","
                        + "\"title\":\"Unauthorized\","
                        + "\"status\":401,"
                        + "\"detail\":\"A valid bearer token is required\"}"
        );
    }
}

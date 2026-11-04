package com.maquigest.platform.iam.infrastructure.authorization.sfs.model;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

/** Reconstructs the authenticated principal from a verified JWT for the current request. */
public final class UsernamePasswordAuthenticationTokenBuilder {
    private UsernamePasswordAuthenticationTokenBuilder() {
    }

    public static UsernamePasswordAuthenticationToken build(UserDetails principal,
                                                            HttpServletRequest request) {
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }
}

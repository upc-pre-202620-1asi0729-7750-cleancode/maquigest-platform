package com.maquigest.platform.iam.interfaces.rest;

import com.maquigest.platform.iam.interfaces.rest.resources.CurrentUserResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated endpoint used to verify SecurityFilterChain and JWT via Swagger. */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "MaquiGest account registration and authentication")
public class AuthenticatedPrincipalController {
    @GetMapping("/me")
    @Operation(summary = "Get authenticated principal", description = "Requires a valid JWT bearer token",
            security = @SecurityRequirement(name = "bearerAuth"))
    public CurrentUserResource currentUser(Authentication authentication) {
        var authorities = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .sorted()
                .toList();
        return new CurrentUserResource(authentication.getName(), authorities);
    }
}

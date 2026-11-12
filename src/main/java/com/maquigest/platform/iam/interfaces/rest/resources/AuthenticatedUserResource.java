package com.maquigest.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/** Successful sign-in response; never contains password or stored hash. */
@Schema(name = "MaquiGestAuthenticatedUserResponse")
public record AuthenticatedUserResource(
        Long id,
        String email,
        String role,
        String status,
        String token
) {
}

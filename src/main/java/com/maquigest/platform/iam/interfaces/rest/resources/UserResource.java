package com.maquigest.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/** Public registration response. Intentionally excludes the password hash. */
@Schema(name = "MaquiGestUserResponse")
public record UserResource(Long id, String firstName, String lastName,
                           String email, String companyName, String role, String status) {
}

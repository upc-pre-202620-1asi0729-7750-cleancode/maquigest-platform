package com.maquigest.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** MaquiGest sign-in request: {email, password}. */
@Schema(name = "MaquiGestSignInRequest", description = "Authenticate an existing company account")
public record SignInResource(
        @NotBlank(message = "{validation.not-blank}")
        @Email
        @Schema(description = "Registered email", example = "carlos.prueba@example.com")
        String email,
        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "Account password", example = "PruebaSegura123!")
        String password
) {
}

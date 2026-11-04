package com.maquigest.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** MaquiGest sign-up payload; mirrors the existing Angular contract. */
@Schema(name = "MaquiGestSignUpRequest", description = "Register a rental or construction company account")
public record SignUpResource(
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 100)
        String firstName,
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 150)
        String lastName,
        @NotBlank(message = "{validation.not-blank}")
        @Email
        @Size(max = 254)
        String email,
        @NotBlank(message = "{validation.not-blank}")
        @Size(min = 8, max = 255)
        String password,
        @NotBlank(message = "{validation.not-blank}")
        @Size(max = 180)
        String companyName,
        @NotBlank(message = "{validation.not-blank}")
        @Schema(description = "rental_company or construction_company", example = "rental_company")
        String role,
        @Schema(description = "Ignored: the server assigns active status to new accounts", example = "active")
        String status
) {
}

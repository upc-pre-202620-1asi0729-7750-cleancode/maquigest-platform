package com.maquigest.platform.iam.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record ResendEmailVerificationResource(@NotBlank @Email @Size(max=254) String email) { }

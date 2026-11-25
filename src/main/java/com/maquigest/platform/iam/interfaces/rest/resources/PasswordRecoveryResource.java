package com.maquigest.platform.iam.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record PasswordRecoveryResource(@NotBlank @Email @Size(max = 254) String email) { }

package com.maquigest.platform.iam.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record ResetPasswordResource(@NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String token,
        @NotBlank @Size(min = 8, max = 72) String password) {
    @Override public String toString() { return "ResetPasswordResource[redacted]"; }
}

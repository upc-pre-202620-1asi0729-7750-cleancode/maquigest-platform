package com.maquigest.platform.iam.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record VerifyEmailResource(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{43}") String token,
    @NotBlank @Size(max=72) String password) {
    @Override public String toString() { return "VerifyEmailResource[redacted]"; }
}

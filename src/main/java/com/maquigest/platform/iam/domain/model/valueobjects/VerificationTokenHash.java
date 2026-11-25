package com.maquigest.platform.iam.domain.model.valueobjects;

/** SHA-256 digest, never the bearer verification token. */
public record VerificationTokenHash(String value) {
    public VerificationTokenHash {
        if (value == null || !value.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Invalid verification token hash");
    }
    @Override public String toString() { return "VerificationTokenHash[redacted]"; }
}

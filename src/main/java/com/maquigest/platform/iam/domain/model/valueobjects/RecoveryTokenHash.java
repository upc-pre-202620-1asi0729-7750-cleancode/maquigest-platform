package com.maquigest.platform.iam.domain.model.valueobjects;

/** SHA-256 digest, never the bearer recovery token. */
public record RecoveryTokenHash(String value) {
    public RecoveryTokenHash {
        if (value == null || !value.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Invalid recovery token hash");
    }
    @Override public String toString() { return "RecoveryTokenHash[redacted]"; }
}

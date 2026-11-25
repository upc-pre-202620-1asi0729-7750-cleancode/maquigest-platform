package com.maquigest.platform.iam.domain.model.commands;
public record VerifyEmailCommand(String token, String password) {
    @Override public String toString() { return "VerifyEmailCommand[redacted]"; }
}

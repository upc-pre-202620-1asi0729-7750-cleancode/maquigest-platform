package com.maquigest.platform.iam.domain.model.commands;
public record ResetPasswordCommand(String token, String password) {
    @Override public String toString() { return "ResetPasswordCommand[redacted]"; }
}

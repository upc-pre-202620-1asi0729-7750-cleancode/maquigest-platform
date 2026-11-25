package com.maquigest.platform.shared.application.email;

/** Provider-independent plain text message; never log its content. */
public record EmailMessage(String recipient, String subject, String body) {
    public EmailMessage {
        if (recipient == null || recipient.isBlank() || subject == null || subject.isBlank()
                || body == null || body.isBlank()) throw new IllegalArgumentException("Email fields required");
    }
    @Override public String toString() { return "EmailMessage[redacted]"; }
}

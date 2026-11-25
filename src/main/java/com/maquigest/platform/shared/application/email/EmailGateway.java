package com.maquigest.platform.shared.application.email;

public interface EmailGateway {
    boolean isEnabled();
    /** Returns only when the provider accepts the message. */
    void send(EmailMessage message);
}

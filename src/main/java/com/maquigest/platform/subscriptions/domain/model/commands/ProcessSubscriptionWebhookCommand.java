package com.maquigest.platform.subscriptions.domain.model.commands;

/** Preserves the exact raw bytes required for signature verification. */
public record ProcessSubscriptionWebhookCommand(byte[] payload, String signature) {
    public ProcessSubscriptionWebhookCommand {
        payload = payload == null ? null : payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload == null ? null : payload.clone();
    }
}

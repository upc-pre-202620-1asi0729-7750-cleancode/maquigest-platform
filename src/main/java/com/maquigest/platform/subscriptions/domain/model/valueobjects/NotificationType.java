package com.maquigest.platform.subscriptions.domain.model.valueobjects;

/** Business facts supported by subscriptions notifications. */
public enum NotificationType {
    SUBSCRIPTION_ACTIVATED, SUBSCRIPTION_CANCELLED;

    public boolean requiresEmail() { return false; }
}

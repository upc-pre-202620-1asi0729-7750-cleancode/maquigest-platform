package com.maquigest.platform.iam.domain.model.valueobjects;

/** Business facts supported by iam notifications. */
public enum NotificationType {
    ACCOUNT_REGISTERED;

    public boolean requiresEmail() { return false; }
}

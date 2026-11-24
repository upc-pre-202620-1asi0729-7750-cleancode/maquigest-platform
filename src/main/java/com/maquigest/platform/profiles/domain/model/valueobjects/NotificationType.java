package com.maquigest.platform.profiles.domain.model.valueobjects;

/** Business facts supported by profiles notifications. */
public enum NotificationType {
    PROFILE_CREATED, PROFILE_UPDATED;

    public boolean requiresEmail() { return false; }
}

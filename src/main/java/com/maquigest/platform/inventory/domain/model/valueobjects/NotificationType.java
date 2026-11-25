package com.maquigest.platform.inventory.domain.model.valueobjects;

/** Business facts supported by inventory notifications. */
public enum NotificationType {
    EQUIPMENT_CREATED, EQUIPMENT_STATUS_CHANGED;

    public boolean requiresEmail() { return false; }
}

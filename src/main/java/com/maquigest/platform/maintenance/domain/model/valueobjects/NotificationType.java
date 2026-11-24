package com.maquigest.platform.maintenance.domain.model.valueobjects;

/** Business facts supported by maintenance notifications. */
public enum NotificationType {
    MAINTENANCE_SCHEDULED, MAINTENANCE_COMPLETED, INCIDENT_REPORTED, INCIDENT_RESOLVED;

    public boolean requiresEmail() { return false; }
}

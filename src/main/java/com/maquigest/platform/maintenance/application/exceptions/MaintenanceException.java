package com.maquigest.platform.maintenance.application.exceptions;

import com.maquigest.platform.shared.application.result.ApplicationError;

/** An expected Maintenance failure. Transport-independent application exception. */
public final class MaintenanceException extends RuntimeException {
    private final String code;
    public MaintenanceException(String code, String description) {
        super(description);
        this.code = code;
    }
    public String code() { return code; }
    public static MaintenanceException fromApplicationError(ApplicationError error) {
        return new MaintenanceException(error.code(), error.message());
    }
    public static MaintenanceException invalid(String description) {
        return new MaintenanceException("MAINTENANCE_INVALID", description);
    }
    public static MaintenanceException forbidden() {
        return new MaintenanceException("MAINTENANCE_FORBIDDEN", "The operation is not allowed for this company");
    }
    public static MaintenanceException notFound(String what) {
        return new MaintenanceException("MAINTENANCE_NOT_FOUND", what + " was not found");
    }
    public static MaintenanceException conflict(String description) {
        return new MaintenanceException("MAINTENANCE_CONFLICT", description);
    }
}

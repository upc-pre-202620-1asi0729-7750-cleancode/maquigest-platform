package com.maquigest.platform.rentals.application.exceptions;

import com.maquigest.platform.shared.application.result.ApplicationError;

/** An expected Rentals failure. Transport-independent application exception. */
public final class RentalsException extends RuntimeException {
    private final String code;
    public RentalsException(String code, String description) {
        super(description);
        this.code = code;
    }
    public String code() { return code; }
    public static RentalsException fromApplicationError(ApplicationError error) {
        return new RentalsException(error.code(), error.message());
    }
    public static RentalsException invalid(String description) {
        return new RentalsException("RENTALS_VALIDATION_ERROR", description);
    }
    public static RentalsException forbidden() {
        return new RentalsException("RENTALS_FORBIDDEN", "Operation is not permitted for this account");
    }
    public static RentalsException notFound(String resource) {
        return new RentalsException("RENTALS_NOT_FOUND", resource + " not found");
    }
    public static RentalsException conflict(String description) {
        return new RentalsException("RENTALS_CONFLICT", description);
    }
}

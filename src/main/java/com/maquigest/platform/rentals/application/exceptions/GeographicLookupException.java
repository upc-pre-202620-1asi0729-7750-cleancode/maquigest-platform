package com.maquigest.platform.rentals.application.exceptions;

/** Expected geographic lookup failure. Never retains the provider exception or response. */
public final class GeographicLookupException extends RuntimeException {
    public enum Code { INVALID, FORBIDDEN, NOT_FOUND, NO_RESULTS, UNAVAILABLE }
    private final Code code;
    public GeographicLookupException(Code code) { super(code.name()); this.code = code; }
    public Code code() { return code; }
}

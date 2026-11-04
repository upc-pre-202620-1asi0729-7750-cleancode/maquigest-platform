package com.maquigest.platform.rentals.application.exceptions;
import org.springframework.http.HttpStatus;
public final class RentalsException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public RentalsException(HttpStatus status, String code, String description) {
        super(description);this.status=status;this.code=code;
    }
    public HttpStatus status(){return status;}
    public String code(){return code;}
    public static RentalsException invalid(String description) {return new RentalsException(HttpStatus.BAD_REQUEST,"RENTALS_VALIDATION_ERROR",description);}
    public static RentalsException forbidden() {return new RentalsException(HttpStatus.FORBIDDEN,"RENTALS_FORBIDDEN","Operation is not permitted for this account");}
    public static RentalsException notFound(String resource) {return new RentalsException(HttpStatus.NOT_FOUND,"RENTALS_NOT_FOUND",resource+" not found");}
    public static RentalsException conflict(String description) {return new RentalsException(HttpStatus.CONFLICT,"RENTALS_CONFLICT",description);}
}

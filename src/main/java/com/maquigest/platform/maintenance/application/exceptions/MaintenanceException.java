package com.maquigest.platform.maintenance.application.exceptions;
import org.springframework.http.HttpStatus;
public final class MaintenanceException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    public MaintenanceException(HttpStatus status,String code,String detail){super(detail);this.status=status;this.code=code;}
    public HttpStatus status(){return status;} public String code(){return code;}
    public static MaintenanceException invalid(String msg){return new MaintenanceException(HttpStatus.BAD_REQUEST,"MAINTENANCE_INVALID",msg);}
    public static MaintenanceException forbidden(){return new MaintenanceException(HttpStatus.FORBIDDEN,"MAINTENANCE_FORBIDDEN","The operation is not allowed for this company");}
    public static MaintenanceException notFound(String what){return new MaintenanceException(HttpStatus.NOT_FOUND,"MAINTENANCE_NOT_FOUND",what+" was not found");}
    public static MaintenanceException conflict(String msg){return new MaintenanceException(HttpStatus.CONFLICT,"MAINTENANCE_CONFLICT",msg);}
}

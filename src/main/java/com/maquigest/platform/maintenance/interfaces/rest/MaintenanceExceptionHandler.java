package com.maquigest.platform.maintenance.interfaces.rest;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import org.springframework.http.*;
import com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.ResourceBundle;
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MaintenanceExceptionHandler {
    @ExceptionHandler(MaintenanceException.class)
    public ResponseEntity<ProblemDetail> handle(MaintenanceException e){
        String title="Maintenance operation rejected";
        try { var bundle=ResourceBundle.getBundle("maintenance",LocaleContextHolder.getLocale());
            title=bundle.getString("maintenance.error.title");}
        catch(java.util.MissingResourceException ignored){}
        var status=ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(e.code());
        var body=ProblemDetail.forStatusAndDetail(status,e.getMessage());
        body.setTitle(title);body.setProperty("code",e.code());
        return ResponseEntity.status(status).body(body);
    }
}

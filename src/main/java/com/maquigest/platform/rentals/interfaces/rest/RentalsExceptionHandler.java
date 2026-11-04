package com.maquigest.platform.rentals.interfaces.rest;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.i18n.LocaleContextHolder;
import java.util.*;
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages="com.maquigest.platform.rentals.interfaces.rest")
public class RentalsExceptionHandler {
    @ExceptionHandler(RentalsException.class)
    public ResponseEntity<ProblemDetail> handle(RentalsException ex) {
        var problem=ProblemDetail.forStatusAndDetail(ex.status(),localize(ex.code(),ex.getMessage()));
        problem.setTitle(ex.status().getReasonPhrase());
        problem.setProperty("code",ex.code());
        return ResponseEntity.status(ex.status()).body(problem);
    }
    private static String localize(String code,String fallback) {
        try {return ResourceBundle.getBundle("rentals",LocaleContextHolder.getLocale()).getString(code);}
        catch(MissingResourceException ignored){return fallback;}
    }
}

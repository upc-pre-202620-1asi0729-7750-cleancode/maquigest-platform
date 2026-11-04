package com.maquigest.platform.profiles.domain.model.valueobjects;
import java.util.Locale;
public record EmailAddress(String value) {
    public EmailAddress {
        if(value==null) throw new IllegalArgumentException("contactEmail is required");
        value=value.trim().toLowerCase(Locale.ROOT);
        if(value.length()>254 || !value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("contactEmail must be a valid email");
    }
}

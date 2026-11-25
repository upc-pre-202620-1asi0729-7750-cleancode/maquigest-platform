package com.maquigest.platform.maintenance.domain.model.valueobjects;

public record TechnicianName(String value) {
    public TechnicianName {
        value = value == null ? "" : value.trim();
        if (value.length() > 180) throw new IllegalArgumentException("Technician name is too long");
    }
}

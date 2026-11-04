package com.maquigest.platform.maintenance.domain.model.valueobjects;
public record MaintenanceType(String value) {
public MaintenanceType { if(value==null||value.isBlank()||value.trim().length()>120) throw new IllegalArgumentException("Maintenance type is required (max 120)"); value=value.trim(); }
}

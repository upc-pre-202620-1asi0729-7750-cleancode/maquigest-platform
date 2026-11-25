package com.maquigest.platform.inventory.domain.model.valueobjects;

public record EquipmentCode(String value) {
    public EquipmentCode {
        if (value == null || value.isBlank() || value.trim().length() > 60) {
            throw new IllegalArgumentException("code is required and must not exceed 60 characters");
        }
        value = value.trim();
    }
}

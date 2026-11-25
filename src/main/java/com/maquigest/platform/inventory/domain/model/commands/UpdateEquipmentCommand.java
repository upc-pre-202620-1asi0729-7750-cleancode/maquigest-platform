package com.maquigest.platform.inventory.domain.model.commands;
import java.math.BigDecimal;
/** Only seven editable equipment fields; no status, owner, or availability blocks. */
public record UpdateEquipmentCommand(Long id, String code, String name,
    String description, Long categoryId, String location, BigDecimal dailyRate, BigDecimal weeklyRate) {
    public UpdateEquipmentCommand {
        if (id == null || id <= 0) throw new IllegalArgumentException("id must be positive");
        if (categoryId == null || categoryId <= 0) throw new IllegalArgumentException("categoryId must be positive");
        new com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentCode(code);
        if (name == null || name.isBlank() || name.trim().length() > 180) throw new IllegalArgumentException("name is required (max 180)");
        if (description == null || description.isBlank() || description.trim().length() > 3000) throw new IllegalArgumentException("description is required (max 3000)");
        if (location == null || location.isBlank() || location.trim().length() > 200) throw new IllegalArgumentException("location is required (max 200)");
        new com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate(dailyRate, weeklyRate);
    }
}

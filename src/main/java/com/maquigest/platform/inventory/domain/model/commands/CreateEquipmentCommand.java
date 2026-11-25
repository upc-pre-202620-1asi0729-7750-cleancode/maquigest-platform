package com.maquigest.platform.inventory.domain.model.commands;
import java.math.BigDecimal;
/** Owner is supplied by the WebApp and verified against the authenticated IAM user at REST boundary. */
public record CreateEquipmentCommand(Long userId, String code, String name,
    String description, Long categoryId, String location, BigDecimal dailyRate, BigDecimal weeklyRate) {
    public CreateEquipmentCommand {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
        if (categoryId == null || categoryId <= 0) throw new IllegalArgumentException("categoryId must be positive");
        new com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentCode(code);
        if (name == null || name.isBlank() || name.trim().length() > 180) throw new IllegalArgumentException("name is required (max 180)");
        if (description == null || description.isBlank() || description.trim().length() > 3000) throw new IllegalArgumentException("description is required (max 3000)");
        if (location == null || location.isBlank() || location.trim().length() > 200) throw new IllegalArgumentException("location is required (max 200)");
        new com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate(dailyRate, weeklyRate);
    }
}

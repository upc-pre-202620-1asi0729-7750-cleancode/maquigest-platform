package com.maquigest.platform.inventory.domain.model.commands;
import java.math.BigDecimal;
/** Only seven editable equipment fields; no status, owner, or availability blocks. */
public record UpdateEquipmentCommand(Long id, String code, String name,
    String description, Long categoryId, String location, BigDecimal dailyRate, BigDecimal weeklyRate) { }

package com.maquigest.platform.inventory.domain.model.commands;
import java.math.BigDecimal;
/** Owner is supplied by the WebApp and verified against the authenticated IAM user at REST boundary. */
public record CreateEquipmentCommand(Long userId, String code, String name,
    String description, Long categoryId, String location, BigDecimal dailyRate, BigDecimal weeklyRate) { }

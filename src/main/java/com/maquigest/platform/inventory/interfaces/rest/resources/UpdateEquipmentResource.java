package com.maquigest.platform.inventory.interfaces.rest.resources;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
/** Exactly the seven fields sent by Angular's PATCH request. */
public record UpdateEquipmentResource(
    @NotBlank @Size(max=60) String code,
    @NotBlank @Size(max=180) String name,
    @NotBlank @Size(max=3000) String description,
    @NotNull @Positive Long categoryId,
    @NotBlank @Size(max=200) String location,
    @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal dailyRate,
    @NotNull @DecimalMin("0.01") @Digits(integer=10,fraction=2) BigDecimal weeklyRate) { }

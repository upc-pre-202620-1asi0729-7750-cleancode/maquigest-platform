package com.maquigest.platform.inventory.interfaces.rest.resources;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import java.math.BigDecimal;
import java.util.List;
/** Exact names from Angular EquipmentResource; do not change without adapting its assembler. */
public record EquipmentResource(Long id, Long userId, String code, String name,
    String description, Long categoryId, String location, BigDecimal dailyRate,
    BigDecimal weeklyRate, EquipmentStatus status, List<AvailabilityBlockResource> availabilityBlocks) { }

package com.maquigest.platform.inventory.domain.model.queries;
/** Null ownerId means all equipment (marketplace); otherwise select owner inventory. */
public record GetAllEquipmentQuery(Long userId) { }

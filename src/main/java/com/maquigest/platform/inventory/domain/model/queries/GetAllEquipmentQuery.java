package com.maquigest.platform.inventory.domain.model.queries;
/** Null ownerId means all equipment (marketplace); otherwise select owner inventory. */
public record GetAllEquipmentQuery(Long userId) {
    public GetAllEquipmentQuery {
        if (userId != null && userId <= 0) throw new IllegalArgumentException("userId must be positive when provided");
    }
}

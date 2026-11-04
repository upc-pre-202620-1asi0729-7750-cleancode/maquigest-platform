package com.maquigest.platform.inventory.domain.model.entities;
/** Catalogue entity; equipment keeps only a categoryId, not a JPA relation. */
public final class EquipmentCategory {
    private final Long id;
    private final String name;
    public EquipmentCategory(Long id, String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Category name is required");
        this.id = id;
        this.name = name.trim();
    }
    public Long getId() { return id; }
    public String getName() { return name; }
}

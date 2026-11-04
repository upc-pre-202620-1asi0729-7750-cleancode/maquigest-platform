package com.maquigest.platform.inventory.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.EquipmentCategoryPersistenceEntity;
public final class EquipmentCategoryPersistenceAssembler {
    private EquipmentCategoryPersistenceAssembler() { }
    public static EquipmentCategory toDomainFromPersistence(EquipmentCategoryPersistenceEntity e) {
        return new EquipmentCategory(e.getId(), e.getName());
    }
    public static EquipmentCategoryPersistenceEntity toPersistenceFromDomain(EquipmentCategory e) {
        var entity = new EquipmentCategoryPersistenceEntity();
        if (e.getId()!=null) entity.setId(e.getId());
        entity.setName(e.getName());
        return entity;
    }
}

package com.maquigest.platform.inventory.interfaces.rest.transform;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import com.maquigest.platform.inventory.interfaces.rest.resources.EquipmentCategoryResource;
public final class EquipmentCategoryResourceFromEntityAssembler {
    private EquipmentCategoryResourceFromEntityAssembler() { }
    public static EquipmentCategoryResource toResourceFromEntity(EquipmentCategory e) {
        return new EquipmentCategoryResource(e.getId(),e.getName());
    }
}

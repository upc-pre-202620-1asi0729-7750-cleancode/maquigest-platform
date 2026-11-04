package com.maquigest.platform.inventory.domain.repositories;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import java.util.List;
public interface EquipmentCategoryRepository {
    List<EquipmentCategory> findAll();
    boolean existsById(Long id);
    boolean existsByName(String name);
    EquipmentCategory save(EquipmentCategory category);
}

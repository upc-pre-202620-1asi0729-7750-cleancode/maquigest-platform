package com.maquigest.platform.inventory.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.EquipmentCategoryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface EquipmentCategoryPersistenceRepository extends JpaRepository<EquipmentCategoryPersistenceEntity,Long> {
    boolean existsByNameIgnoreCase(String name);
}

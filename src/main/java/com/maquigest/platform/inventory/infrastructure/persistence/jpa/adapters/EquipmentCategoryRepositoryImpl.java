package com.maquigest.platform.inventory.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import com.maquigest.platform.inventory.domain.repositories.EquipmentCategoryRepository;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.assemblers.EquipmentCategoryPersistenceAssembler;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.repositories.EquipmentCategoryPersistenceRepository;
import org.springframework.stereotype.Repository;
import java.util.Comparator;
import java.util.List;
@Repository
public class EquipmentCategoryRepositoryImpl implements EquipmentCategoryRepository {
    private final EquipmentCategoryPersistenceRepository persistence;
    public EquipmentCategoryRepositoryImpl(EquipmentCategoryPersistenceRepository persistence) { this.persistence=persistence; }
    @Override public List<EquipmentCategory> findAll() {
        return persistence.findAll().stream().sorted(Comparator.comparing(e->e.getId()))
            .map(EquipmentCategoryPersistenceAssembler::toDomainFromPersistence).toList();
    }
    @Override public boolean existsById(Long id) { return id!=null && persistence.existsById(id); }
    @Override public boolean existsByName(String name) { return persistence.existsByNameIgnoreCase(name); }
    @Override public EquipmentCategory save(EquipmentCategory category) {
        return EquipmentCategoryPersistenceAssembler.toDomainFromPersistence(
            persistence.saveAndFlush(EquipmentCategoryPersistenceAssembler.toPersistenceFromDomain(category)));
    }
}

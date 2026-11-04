package com.maquigest.platform.inventory.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.assemblers.EquipmentPersistenceAssembler;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.repositories.EquipmentPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
@Repository
public class EquipmentRepositoryImpl implements EquipmentRepository {
    private final EquipmentPersistenceRepository persistence;
    private final DomainEventPublisher publisher;
    public EquipmentRepositoryImpl(EquipmentPersistenceRepository persistence,DomainEventPublisher publisher) {
        this.persistence=persistence; this.publisher=publisher;
    }
    @Override public Optional<Equipment> findById(Long id) {
        return persistence.findById(id).map(EquipmentPersistenceAssembler::toDomainFromPersistence);
    }
    @Override public List<Equipment> findAll() {
        return persistence.findAll().stream().sorted(Comparator.comparing(EquipmentPersistenceEntity::getId))
            .map(EquipmentPersistenceAssembler::toDomainFromPersistence).toList();
    }
    @Override public List<Equipment> findByUserId(Long userId) {
        return persistence.findByUserIdOrderByIdAsc(userId).stream()
            .map(EquipmentPersistenceAssembler::toDomainFromPersistence).toList();
    }
    @Override public Equipment save(Equipment equipment) {
        EquipmentPersistenceEntity entity = equipment.getId()==null
            ? EquipmentPersistenceAssembler.toNewPersistenceFromDomain(equipment)
            : persistence.findById(equipment.getId()).orElseThrow(
                () -> new IllegalArgumentException("Equipment no longer exists"));
        if (equipment.getId()!=null) EquipmentPersistenceAssembler.copyToExistingPersistence(equipment,entity);
        var saved = persistence.saveAndFlush(entity);
        publisher.publishAndClearEvents(equipment);
        return EquipmentPersistenceAssembler.toDomainFromPersistence(saved);
    }
    @Override public boolean existsByUserIdAndCodeIgnoreCase(Long userId,String code) {
        return persistence.existsByUserIdAndCodeIgnoreCase(userId, code);
    }
    @Override public boolean existsByUserIdAndCodeIgnoreCaseAndIdNot(Long userId,String code,Long id) {
        return persistence.existsByUserIdAndCodeIgnoreCaseAndIdNot(userId, code, id);
    }
    @Override public void deleteById(Long id) { persistence.deleteById(id); }
}

package com.maquigest.platform.inventory.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.*;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/** Anti-corruption mapping: JPA never enters Domain. */
public final class EquipmentPersistenceAssembler {
    private EquipmentPersistenceAssembler() { }
    public static Equipment toDomainFromPersistence(EquipmentPersistenceEntity entity) {
        return Equipment.reconstitute(entity.getId(),entity.getUserId(),entity.getCode(),entity.getName(),
            entity.getDescription(),entity.getCategoryId(),entity.getLocation(),
            new RentalRate(entity.getDailyRate(),entity.getWeeklyRate()),entity.getStatus(),
            entity.getAvailabilityBlocks().stream().map(b -> new AvailabilityBlock(
                b.getId(),b.getStartDate(),b.getEndDate(),b.getReleasedAt())).toList());
    }
    public static EquipmentPersistenceEntity toNewPersistenceFromDomain(Equipment equipment) {
        var entity = new EquipmentPersistenceEntity();
        copyToExistingPersistence(equipment,entity);
        return entity;
    }
    public static void copyToExistingPersistence(Equipment equipment, EquipmentPersistenceEntity entity) {
        entity.setUserId(equipment.getUserId());
        entity.setCode(equipment.getCode());
        entity.setName(equipment.getName());
        entity.setDescription(equipment.getDescription());
        entity.setCategoryId(equipment.getCategoryId());
        entity.setLocation(equipment.getLocation());
        entity.setDailyRate(equipment.getRentalRate().dailyRate());
        entity.setWeeklyRate(equipment.getRentalRate().weeklyRate());
        entity.setStatus(equipment.getStatus());
        // Preserve persisted child entity identities on normal PATCH updates.
        Map<Long,AvailabilityBlockPersistenceEntity> currentById = entity.getAvailabilityBlocks().stream()
            .filter(b -> b.getId()!=null).collect(Collectors.toMap(
                AvailabilityBlockPersistenceEntity::getId,Function.identity()));
        // Never orphan-delete historical blocks. Release is represented by releasedAt.
        var desired = equipment.getAllAvailabilityBlocks();
        for (var domainBlock : desired) {
            var existing = domainBlock.getId()==null ? null : currentById.get(domainBlock.getId());
            if (existing==null) {
                existing = new AvailabilityBlockPersistenceEntity();
                entity.addAvailabilityBlock(existing);
            }
            existing.setStartDate(domainBlock.getStartDate());
            existing.setEndDate(domainBlock.getEndDate());
            existing.setReleasedAt(domainBlock.getReleasedAt());
        }
    }
}

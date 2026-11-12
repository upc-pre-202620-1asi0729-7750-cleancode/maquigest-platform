package com.maquigest.platform.inventory.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface EquipmentPersistenceRepository extends JpaRepository<EquipmentPersistenceEntity, Long> {
    @Override @EntityGraph(attributePaths="availabilityBlocks")
    Optional<EquipmentPersistenceEntity> findById(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EquipmentPersistenceEntity e where e.id = :id")
    Optional<EquipmentPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    @Override @EntityGraph(attributePaths="availabilityBlocks")
    List<EquipmentPersistenceEntity> findAll();
    @EntityGraph(attributePaths="availabilityBlocks")
    List<EquipmentPersistenceEntity> findByUserIdOrderByIdAsc(Long userId);
    boolean existsByUserIdAndCodeIgnoreCase(Long userId, String code);
    boolean existsByUserIdAndCodeIgnoreCaseAndIdNot(Long userId, String code, Long id);
}

package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenancePersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface MaintenanceJpaRepository extends JpaRepository<MaintenancePersistenceEntity,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MaintenancePersistenceEntity m where m.id=:id")
    Optional<MaintenancePersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    List<MaintenancePersistenceEntity> findByOwnerUserIdOrderByPerformedAtDesc(Long ownerUserId);
    List<MaintenancePersistenceEntity> findByEquipmentIdOrderByPerformedAtDesc(Long equipmentId);
}

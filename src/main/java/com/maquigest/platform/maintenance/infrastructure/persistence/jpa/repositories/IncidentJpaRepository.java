package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface IncidentJpaRepository extends JpaRepository<IncidentPersistenceEntity,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IncidentPersistenceEntity i where i.id=:id")
    Optional<IncidentPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    List<IncidentPersistenceEntity> findByOwnerUserIdOrderByReportedAtDesc(Long ownerUserId);
    List<IncidentPersistenceEntity> findByEquipmentIdOrderByReportedAtDesc(Long equipmentId);
    boolean existsByEquipmentIdAndBlocksRentalTrueAndStatus(Long equipmentId,IncidentStatus status);
    boolean existsByEquipmentIdAndBlocksRentalTrue(Long equipmentId);
}

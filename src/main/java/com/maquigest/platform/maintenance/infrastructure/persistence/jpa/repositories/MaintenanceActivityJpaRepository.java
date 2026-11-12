package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities.MaintenanceActivityPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MaintenanceActivityJpaRepository extends JpaRepository<MaintenanceActivityPersistenceEntity,Long> {
    List<MaintenanceActivityPersistenceEntity> findByMaintenanceIdOrderByOccurredAtAsc(Long maintenanceId);
}

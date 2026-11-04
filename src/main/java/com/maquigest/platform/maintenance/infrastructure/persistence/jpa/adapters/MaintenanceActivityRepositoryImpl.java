package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.repositories.MaintenanceActivityRepository;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceActivityJpaRepository;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.assemblers.MaintenanceActivityPersistenceAssembler;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class MaintenanceActivityRepositoryImpl implements MaintenanceActivityRepository {
    private final MaintenanceActivityJpaRepository jpa;
    public MaintenanceActivityRepositoryImpl(MaintenanceActivityJpaRepository jpa){this.jpa=jpa;}
    public MaintenanceActivity save(MaintenanceActivity item){return MaintenanceActivityPersistenceAssembler.toDomain(jpa.saveAndFlush(MaintenanceActivityPersistenceAssembler.toNew(item)));}
    public List<MaintenanceActivity> findByMaintenanceId(Long id){return jpa.findByMaintenanceIdOrderByOccurredAtAsc(id).stream().map(MaintenanceActivityPersistenceAssembler::toDomain).toList();}
}

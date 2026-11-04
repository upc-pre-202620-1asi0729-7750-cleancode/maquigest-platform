package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.repositories.MaintenanceRepository;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceJpaRepository;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.assemblers.MaintenancePersistenceAssembler;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class MaintenanceRepositoryImpl implements MaintenanceRepository {
    private final MaintenanceJpaRepository jpa;private final DomainEventPublisher events;
    public MaintenanceRepositoryImpl(MaintenanceJpaRepository jpa,DomainEventPublisher events){this.jpa=jpa;this.events=events;}
    public Optional<Maintenance> findById(Long id){return jpa.findById(id).map(MaintenancePersistenceAssembler::toDomain);}
    public Optional<Maintenance> findByIdForUpdate(Long id){return jpa.findByIdForUpdate(id).map(MaintenancePersistenceAssembler::toDomain);}
    public List<Maintenance> findByOwner(Long owner){return jpa.findByOwnerUserIdOrderByPerformedAtDesc(owner).stream().map(MaintenancePersistenceAssembler::toDomain).toList();}
    public List<Maintenance> findByEquipmentId(Long id){return jpa.findByEquipmentIdOrderByPerformedAtDesc(id).stream().map(MaintenancePersistenceAssembler::toDomain).toList();}
    public Maintenance save(Maintenance maintenance){
        var entity=maintenance.getId()==null?MaintenancePersistenceAssembler.toNew(maintenance):jpa.findById(maintenance.getId())
            .orElseThrow(()->new IllegalArgumentException("Maintenance not found"));
        if(maintenance.getId()!=null)MaintenancePersistenceAssembler.copy(maintenance,entity);
        var saved=jpa.saveAndFlush(entity);events.publishAndClearEvents(maintenance);
        return MaintenancePersistenceAssembler.toDomain(saved);
    }
}

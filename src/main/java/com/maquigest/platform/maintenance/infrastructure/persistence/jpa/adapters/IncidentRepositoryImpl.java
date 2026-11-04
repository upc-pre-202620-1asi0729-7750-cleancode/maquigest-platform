package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import com.maquigest.platform.maintenance.domain.repositories.IncidentRepository;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.repositories.IncidentJpaRepository;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.assemblers.IncidentPersistenceAssembler;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class IncidentRepositoryImpl implements IncidentRepository {
    private final IncidentJpaRepository jpa;private final DomainEventPublisher events;
    public IncidentRepositoryImpl(IncidentJpaRepository jpa,DomainEventPublisher events){this.jpa=jpa;this.events=events;}
    public Optional<Incident> findById(Long id){return jpa.findById(id).map(IncidentPersistenceAssembler::toDomain);}
    public Optional<Incident> findByIdForUpdate(Long id){return jpa.findByIdForUpdate(id).map(IncidentPersistenceAssembler::toDomain);}
    public List<Incident> findByOwner(Long owner){return jpa.findByOwnerUserIdOrderByReportedAtDesc(owner).stream().map(IncidentPersistenceAssembler::toDomain).toList();}
    public List<Incident> findByEquipmentId(Long id){return jpa.findByEquipmentIdOrderByReportedAtDesc(id).stream().map(IncidentPersistenceAssembler::toDomain).toList();}
    public boolean hasOpenBlocking(Long id){return jpa.existsByEquipmentIdAndBlocksRentalTrueAndStatus(id,IncidentStatus.OPEN);}
    public boolean hadBlocking(Long id){return jpa.existsByEquipmentIdAndBlocksRentalTrue(id);}
    public Incident save(Incident incident){
        var entity=incident.getId()==null?IncidentPersistenceAssembler.toNew(incident):jpa.findById(incident.getId())
            .orElseThrow(()->new IllegalArgumentException("Incident not found"));
        if(incident.getId()!=null)IncidentPersistenceAssembler.copy(incident,entity);
        var saved=jpa.saveAndFlush(entity);events.publishAndClearEvents(incident);
        return IncidentPersistenceAssembler.toDomain(saved);
    }
}

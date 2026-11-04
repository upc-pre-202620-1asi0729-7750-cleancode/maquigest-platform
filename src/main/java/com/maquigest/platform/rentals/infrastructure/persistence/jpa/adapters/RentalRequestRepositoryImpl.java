package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.repositories.RentalRequestRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers.RentalRequestPersistenceAssembler;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.RentalRequestJpaRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class RentalRequestRepositoryImpl implements RentalRequestRepository {
    private final RentalRequestJpaRepository jpa; private final DomainEventPublisher publisher;
    public RentalRequestRepositoryImpl(RentalRequestJpaRepository jpa,DomainEventPublisher publisher){this.jpa=jpa;this.publisher=publisher;}
    public Optional<RentalRequest> findById(Long id){return jpa.findById(id).map(RentalRequestPersistenceAssembler::toDomain);}
    public Optional<RentalRequest> findByIdForUpdate(Long id){return jpa.findByIdForUpdate(id).map(RentalRequestPersistenceAssembler::toDomain);}
    public List<RentalRequest> findVisibleTo(Long actorId){return jpa.findByConstructionUserIdOrRentalCompanyUserIdOrderByIdDesc(actorId,actorId)
        .stream().map(RentalRequestPersistenceAssembler::toDomain).toList();}
    public RentalRequest save(RentalRequest request){
        var entity=request.getId()==null? RentalRequestPersistenceAssembler.toNewPersistence(request) :
            jpa.findById(request.getId()).orElseThrow();
        entity.setStatus(request.getStatus());
        var saved=jpa.saveAndFlush(entity);
        publisher.publishAndClearEvents(request);
        return RentalRequestPersistenceAssembler.toDomain(saved);
    }
}

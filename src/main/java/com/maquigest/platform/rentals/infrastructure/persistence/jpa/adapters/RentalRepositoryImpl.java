package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers.RentalPersistenceAssembler;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.RentalJpaRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class RentalRepositoryImpl implements RentalRepository {
    private final RentalJpaRepository jpa; private final DomainEventPublisher publisher;
    public RentalRepositoryImpl(RentalJpaRepository jpa,DomainEventPublisher publisher){this.jpa=jpa;this.publisher=publisher;}
    public Optional<Rental> findById(Long id){return jpa.findById(id).map(RentalPersistenceAssembler::toDomain);}
    public Optional<Rental> findByIdForUpdate(Long id){return jpa.findByIdForUpdate(id).map(RentalPersistenceAssembler::toDomain);}
    public Optional<Rental> findByRequestId(Long requestId){return jpa.findByRequestId(requestId).map(RentalPersistenceAssembler::toDomain);}
    public List<Rental> findVisibleTo(Long actorId){return jpa.findByConstructionUserIdOrRentalCompanyUserIdOrderByIdDesc(actorId,actorId)
        .stream().map(RentalPersistenceAssembler::toDomain).toList();}
    public Rental save(Rental rental){
        var entity=rental.getId()==null? RentalPersistenceAssembler.toNewPersistence(rental) :
            jpa.findById(rental.getId()).orElseThrow();
        entity.setStatus(rental.getStatus());
        var saved=jpa.saveAndFlush(entity);
        publisher.publishAndClearEvents(rental);
        return RentalPersistenceAssembler.toDomain(saved);
    }
}

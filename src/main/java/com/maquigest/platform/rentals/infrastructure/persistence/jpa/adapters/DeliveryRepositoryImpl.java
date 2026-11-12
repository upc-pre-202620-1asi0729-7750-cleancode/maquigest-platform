package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.repositories.DeliveryRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers.DeliveryPersistenceAssembler;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.DeliveryJpaRepository;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class DeliveryRepositoryImpl implements DeliveryRepository {
    private final DeliveryJpaRepository jpa;
    public DeliveryRepositoryImpl(DeliveryJpaRepository jpa){this.jpa=jpa;}
    public Optional<Delivery> findByRentalId(Long id){return jpa.findByRentalId(id).map(DeliveryPersistenceAssembler::toDomain);}
    public List<Delivery> findByRentalIds(Collection<Long> ids){return ids.isEmpty()?List.of():
        jpa.findByRentalIdIn(ids).stream().map(DeliveryPersistenceAssembler::toDomain).toList();}
    public Delivery save(Delivery delivery){return DeliveryPersistenceAssembler.toDomain(jpa.saveAndFlush(
        DeliveryPersistenceAssembler.toNewPersistence(delivery)));}
}

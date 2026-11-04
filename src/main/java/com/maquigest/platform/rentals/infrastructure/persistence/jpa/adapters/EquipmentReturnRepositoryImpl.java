package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.domain.repositories.EquipmentReturnRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers.EquipmentReturnPersistenceAssembler;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.EquipmentReturnJpaRepository;
import java.util.*;
import org.springframework.stereotype.Repository;
@Repository
public class EquipmentReturnRepositoryImpl implements EquipmentReturnRepository {
    private final EquipmentReturnJpaRepository jpa;
    public EquipmentReturnRepositoryImpl(EquipmentReturnJpaRepository jpa){this.jpa=jpa;}
    public Optional<EquipmentReturn> findByRentalId(Long id){return jpa.findByRentalId(id).map(EquipmentReturnPersistenceAssembler::toDomain);}
    public List<EquipmentReturn> findByRentalIds(Collection<Long> ids){return ids.isEmpty()?List.of():
        jpa.findByRentalIdIn(ids).stream().map(EquipmentReturnPersistenceAssembler::toDomain).toList();}
    public EquipmentReturn save(EquipmentReturn item){return EquipmentReturnPersistenceAssembler.toDomain(jpa.saveAndFlush(
        EquipmentReturnPersistenceAssembler.toNewPersistence(item)));}
}

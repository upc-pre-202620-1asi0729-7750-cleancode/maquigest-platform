package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.EquipmentReturnPersistenceEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface EquipmentReturnJpaRepository extends JpaRepository<EquipmentReturnPersistenceEntity,Long> {

    Optional<EquipmentReturnPersistenceEntity> findByRentalId(Long rentalId);
    List<EquipmentReturnPersistenceEntity> findByRentalIdIn(Collection<Long> ids);

}

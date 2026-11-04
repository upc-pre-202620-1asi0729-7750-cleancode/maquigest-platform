package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.DeliveryPersistenceEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface DeliveryJpaRepository extends JpaRepository<DeliveryPersistenceEntity,Long> {

    Optional<DeliveryPersistenceEntity> findByRentalId(Long rentalId);
    List<DeliveryPersistenceEntity> findByRentalIdIn(Collection<Long> ids);

}

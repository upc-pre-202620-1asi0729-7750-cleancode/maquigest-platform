package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalRequestPersistenceEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RentalRequestJpaRepository extends JpaRepository<RentalRequestPersistenceEntity,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RentalRequestPersistenceEntity r where r.id=:id")
    Optional<RentalRequestPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    List<RentalRequestPersistenceEntity> findByConstructionUserIdOrRentalCompanyUserIdOrderByIdDesc(Long constructionUserId,Long rentalCompanyUserId);

}

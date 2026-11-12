package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalPersistenceEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface RentalJpaRepository extends JpaRepository<RentalPersistenceEntity,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RentalPersistenceEntity r where r.id=:id")
    Optional<RentalPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    Optional<RentalPersistenceEntity> findByRequestId(Long requestId);
    List<RentalPersistenceEntity> findByConstructionUserIdOrRentalCompanyUserIdOrderByIdDesc(Long constructionUserId,Long rentalCompanyUserId);

}

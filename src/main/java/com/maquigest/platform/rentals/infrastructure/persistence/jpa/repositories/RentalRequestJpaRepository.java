package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;

import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalRequestPersistenceEntity;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RentalRequestJpaRepository extends JpaRepository<RentalRequestPersistenceEntity,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RentalRequestPersistenceEntity r where r.id=:id")
    Optional<RentalRequestPersistenceEntity> findByIdForUpdate(@Param("id") Long id);

    List<RentalRequestPersistenceEntity> findByConstructionUserIdOrRentalCompanyUserIdOrderByIdDesc(
        Long constructionUserId, Long rentalCompanyUserId);

    @Query("""
        select count(r) from RentalRequestPersistenceEntity r
        where r.equipmentId = :equipmentId
          and r.constructionUserId = :requesterUserId
          and r.status in :statuses
          and r.startDate <= :endDate
          and r.endDate >= :startDate
          and r.id <> :excludedId
        """)
    long countOverlappingActiveRequests(
        @Param("equipmentId") Long equipmentId,
        @Param("requesterUserId") Long requesterUserId,
        @Param("statuses") Collection<RentalRequestStatus> statuses,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludedId") Long excludedId);
}

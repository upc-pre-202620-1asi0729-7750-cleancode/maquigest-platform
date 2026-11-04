package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;

import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalRequestSubmissionGuardPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acquires an InnoDB row lock even if the pair did not exist before the request.
 * Requires the UNIQUE (equipment_id, construction_user_id) database constraint.
 */
public interface RentalRequestSubmissionGuardJpaRepository
        extends JpaRepository<RentalRequestSubmissionGuardPersistenceEntity, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO rental_request_submission_guards (equipment_id, construction_user_id)
        VALUES (:equipmentId, :requesterUserId)
        ON DUPLICATE KEY UPDATE equipment_id = equipment_id
        """, nativeQuery = true)
    void acquirePairLock(@Param("equipmentId") Long equipmentId,
                         @Param("requesterUserId") Long requesterUserId);
}

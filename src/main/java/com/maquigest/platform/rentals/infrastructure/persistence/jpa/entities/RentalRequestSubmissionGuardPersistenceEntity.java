package com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Technical row allowing atomic request serialization per equipment and requester.
 * It is NOT a business aggregate and must not be exposed from Rentals' domain.
 */
@Entity
@Table(name = "rental_request_submission_guards",
    uniqueConstraints = @UniqueConstraint(name = "uk_rental_request_submission_guard_pair",
        columnNames = {"equipment_id", "construction_user_id"}))
public class RentalRequestSubmissionGuardPersistenceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "construction_user_id", nullable = false)
    private Long constructionUserId;

    protected RentalRequestSubmissionGuardPersistenceEntity() { }
}

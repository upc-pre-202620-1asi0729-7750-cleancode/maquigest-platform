package com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
@Entity @Table(name="rental_requests") @Getter @Setter @NoArgsConstructor
public class RentalRequestPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false) private Long equipmentId;
    @Column(nullable=false) private Long constructionUserId;
    @Column(nullable=false) private Long rentalCompanyUserId;
    @Column(nullable=false) private LocalDate startDate;
    @Column(nullable=false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private RentalRequestStatus status;
    @Column(nullable=false,updatable=false) private Instant createdAt;

}

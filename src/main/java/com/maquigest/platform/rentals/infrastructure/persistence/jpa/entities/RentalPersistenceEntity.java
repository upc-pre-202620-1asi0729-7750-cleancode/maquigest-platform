package com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
@Entity @Table(name="rentals") @Getter @Setter @NoArgsConstructor
public class RentalPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false,unique=true) private Long requestId;
    @Column(nullable=false) private Long equipmentId;
    @Column(nullable=false) private Long constructionUserId;
    @Column(nullable=false) private Long rentalCompanyUserId;
    @Column(nullable=false) private Long availabilityBlockId;
    @Column(nullable=false) private LocalDate startDate;
    @Column(nullable=false) private LocalDate endDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private RentalStatus status;

}

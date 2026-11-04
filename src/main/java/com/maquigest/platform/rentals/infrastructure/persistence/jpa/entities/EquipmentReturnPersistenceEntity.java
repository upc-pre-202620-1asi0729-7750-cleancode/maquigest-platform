package com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="equipment_returns") @Getter @Setter @NoArgsConstructor
public class EquipmentReturnPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false,unique=true) private Long rentalId;
    @Column(nullable=false) private Instant returnedAt;
    @Column(nullable=false,length=2000) private String notes;
    @Column(nullable=false) private boolean maintenanceRequired;

}

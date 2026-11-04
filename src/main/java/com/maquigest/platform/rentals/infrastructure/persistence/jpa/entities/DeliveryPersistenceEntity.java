package com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.*;

@Entity @Table(name="deliveries") @Getter @Setter @NoArgsConstructor
public class DeliveryPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false,unique=true) private Long rentalId;
    @Column(nullable=false) private Instant deliveredAt;
    @Column(nullable=false,length=2000) private String notes;

}

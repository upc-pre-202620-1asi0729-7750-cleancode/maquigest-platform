package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
@Entity @Table(name="maintenance_incidents",indexes={@Index(name="idx_mi_equipment",columnList="equipment_id"),@Index(name="idx_mi_owner",columnList="owner_user_id")})
@Getter @Setter @NoArgsConstructor
public class IncidentPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="equipment_id",nullable=false) private Long equipmentId;
    @Column(name="owner_user_id",nullable=false) private Long ownerUserId;
    @Column(nullable=false,length=3000) private String description;
    @Column(name="reported_at",nullable=false) private Instant reportedAt;
    @Column(name="blocks_rental",nullable=false) private boolean blocksRental;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private IncidentStatus status;
    @Column(name="resolved_at") private Instant resolvedAt;
    @Version private Long version;
}

package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
@Entity @Table(name="maintenance_records",indexes={@Index(name="idx_mr_equipment",columnList="equipment_id"),@Index(name="idx_mr_owner",columnList="owner_user_id")})
@Getter @Setter @NoArgsConstructor
public class MaintenancePersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="equipment_id",nullable=false) private Long equipmentId;
    @Column(name="owner_user_id",nullable=false) private Long ownerUserId;
    @Column(name="performed_at",nullable=false) private Instant performedAt;
    @Column(nullable=false,length=120) private String type;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private MaintenanceStatus status;
    @Column(length=180,nullable=false) private String technician="";
    @Column(name="acknowledged_at") private Instant acknowledgedAt;
    @Version private Long version;
}

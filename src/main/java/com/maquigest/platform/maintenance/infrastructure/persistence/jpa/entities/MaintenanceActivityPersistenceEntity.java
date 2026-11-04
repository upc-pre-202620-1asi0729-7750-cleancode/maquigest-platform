package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
@Entity @Table(name="maintenance_activities",indexes=@Index(name="idx_ma_maintenance",columnList="maintenance_id"))
@Getter @Setter @NoArgsConstructor
public class MaintenanceActivityPersistenceEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="maintenance_id",nullable=false) private Long maintenanceId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private MaintenanceStatus status;
    @Column(name="occurred_at",nullable=false) private Instant occurredAt;
    @Column(nullable=false,length=180) private String technician="";
    @Column(nullable=false,length=2000) private String notes="";
}

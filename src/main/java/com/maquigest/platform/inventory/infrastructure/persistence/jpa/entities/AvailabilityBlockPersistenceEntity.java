package com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
/** Availability block belongs to exactly one Equipment aggregate. */
@Entity
@Table(name="equipment_availability_blocks")
@Getter @Setter @NoArgsConstructor
public class AvailabilityBlockPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="equipment_id",nullable=false)
    private EquipmentPersistenceEntity equipment;
    @Column(name="start_date",nullable=false) private LocalDate startDate;
    @Column(name="end_date",nullable=false) private LocalDate endDate;
}

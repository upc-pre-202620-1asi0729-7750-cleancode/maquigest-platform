package com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name="equipment_categories")
@Getter @Setter @NoArgsConstructor
public class EquipmentCategoryPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false,unique=true,length=120) private String name;
}

package com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/** JPA model is separate from the Equipment aggregate. */
@Entity
@Table(name="equipment", uniqueConstraints=@UniqueConstraint(name="uk_equipment_owner_code", columnNames={"user_id","code"}))
@Getter @Setter @NoArgsConstructor
public class EquipmentPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(nullable=false,length=60) private String code;
    @Column(nullable=false,length=180) private String name;
    @Column(nullable=false,length=3000) private String description;
    @Column(name="category_id",nullable=false) private Long categoryId;
    @Column(nullable=false,length=200) private String location;
    @Column(name="daily_rate",nullable=false,precision=12,scale=2) private BigDecimal dailyRate;
    @Column(name="weekly_rate",nullable=false,precision=12,scale=2) private BigDecimal weeklyRate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private EquipmentStatus status;
    @Version @Column(nullable=false) private Long version;
    @OneToMany(mappedBy="equipment",cascade=CascadeType.ALL,orphanRemoval=true,fetch=FetchType.LAZY)
    @OrderBy("startDate ASC")
    private List<AvailabilityBlockPersistenceEntity> availabilityBlocks = new ArrayList<>();
    public void addAvailabilityBlock(AvailabilityBlockPersistenceEntity block) {
        block.setEquipment(this);
        availabilityBlocks.add(block);
    }
}

package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity @Table(name="subscription_plans", uniqueConstraints=@UniqueConstraint(name="uk_subscription_plans_name", columnNames="name"))
@Getter @Setter @NoArgsConstructor
public class SubscriptionPlanPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false,length=100) private String name;
    @Column(nullable=false,length=1000) private String description;
    @Column(name="price_amount",nullable=false,precision=12,scale=2) private BigDecimal priceAmount;
    @Column(name="price_currency",nullable=false,length=3) private String priceCurrency;
    @Column(name="billing_cycle",nullable=false,length=30) private String billingCycle;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private PlanStatus status;
}

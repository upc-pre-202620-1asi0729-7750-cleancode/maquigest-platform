package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

/** Keep IAM identifiers, not foreign aggregate objects, inside this bounded context. */
@Entity @Table(name="user_subscriptions", indexes={
    @Index(name="idx_user_subscriptions_owner",columnList="user_id"),
    @Index(name="idx_user_subscriptions_active",columnList="user_id,status,end_date")
})
@Getter @Setter @NoArgsConstructor
public class UserSubscriptionPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(name="plan_id",nullable=false) private Long planId;
    @Column(name="start_date",nullable=false) private Instant startDate;
    @Column(name="end_date",nullable=false) private Instant endDate;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private SubscriptionStatus status;
    @Column(name="auto_renew",nullable=false) private boolean autoRenew;
    @Version @Column(nullable=false) private Long version;
}

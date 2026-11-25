package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity
@Table(name="subscription_checkouts",indexes=@Index(name="idx_checkout_owner_status",columnList="user_id,status"))
@Getter @Setter
public class SubscriptionCheckoutPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false,unique=true,length=36) private String reference;
    @Column(name="user_id",nullable=false) private Long userId;
    @Column(nullable=false) private Long planId;
    @Column(nullable=false) private long amountMinor;
    @Column(nullable=false) private Instant expiresAt;
    @Column(unique=true,length=255) private String sessionId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private CheckoutStatus status;
    @Column(unique=true) private Long subscriptionId;
    @Version private Long version;
}

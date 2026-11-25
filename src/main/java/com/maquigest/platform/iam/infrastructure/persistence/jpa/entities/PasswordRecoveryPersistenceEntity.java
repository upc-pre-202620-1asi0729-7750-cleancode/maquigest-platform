package com.maquigest.platform.iam.infrastructure.persistence.jpa.entities;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "password_recoveries")
@Getter @Setter
public class PasswordRecoveryPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false, unique = true) private Long userId;
    @Column(nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(nullable = false) private Instant issuedAt;
    @Column(nullable = false) private Instant expiresAt;
    private Instant consumedAt;
    @Version private Long version;
}

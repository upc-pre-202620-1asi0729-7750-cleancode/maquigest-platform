package com.maquigest.platform.maintenance.infrastructure.persistence.jpa.entities;

import com.maquigest.platform.maintenance.domain.model.valueobjects.NotificationEmailStatus;
import com.maquigest.platform.maintenance.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.maintenance.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.maintenance.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.converters.NotificationRecipientIdPersistenceConverter;
import com.maquigest.platform.maintenance.infrastructure.persistence.jpa.converters.NotificationReferenceIdPersistenceConverter;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;

/** Persistence model for Maintenance notifications, with context-specific names. */
@Entity(name = "MaintenanceNotificationPersistenceEntity")
@Table(name = "maintenance_notifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_maintenance_notification_fact_recipient",
                columnNames = {"recipient_id", "fact_id"})
}, indexes = {
        @Index(name = "idx_maintenance_notifications_recipient", columnList = "recipient_id,occurred_at")
})
@Getter
@Setter
@NoArgsConstructor
public class NotificationPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Convert(converter = NotificationRecipientIdPersistenceConverter.class)
    @Column(name = "recipient_id", nullable = false, updatable = false)
    private NotificationRecipientId recipientId;
    @Convert(converter = NotificationReferenceIdPersistenceConverter.class)
    @Column(name = "reference_id", nullable = false, updatable = false)
    private NotificationReferenceId referenceId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 40)
    private NotificationType type;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
    @Column(name = "fact_id", nullable = false, updatable = false, length = 36)
    private String factId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationEmailStatus emailStatus;
    @Column(name = "read_at")
    private Instant readAt;
    @Version
    @Column(nullable = false)
    private Long version;
}

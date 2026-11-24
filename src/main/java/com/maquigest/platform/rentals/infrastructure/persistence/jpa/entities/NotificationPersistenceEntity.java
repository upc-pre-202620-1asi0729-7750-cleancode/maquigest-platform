package com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities;

import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationEmailStatus;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.converters.NotificationRecipientIdPersistenceConverter;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.converters.NotificationReferenceIdPersistenceConverter;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;

/** Persistence model for Rentals notifications, with context-specific names. */
@Entity(name = "RentalNotificationPersistenceEntity")
@Table(name = "rental_notifications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rental_notification_fact_recipient",
                columnNames = {"recipient_id", "reference_id", "type"})
}, indexes = {
        @Index(name = "idx_rental_notifications_recipient", columnList = "recipient_id,occurred_at")
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
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationEmailStatus emailStatus;
    @Column(name = "read_at")
    private Instant readAt;
    @Version
    @Column(nullable = false)
    private Long version;
}

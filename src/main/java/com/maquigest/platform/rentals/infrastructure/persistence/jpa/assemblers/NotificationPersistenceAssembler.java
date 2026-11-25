package com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers;

import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;

public final class NotificationPersistenceAssembler {
    private NotificationPersistenceAssembler() { }

    public static Notification toDomain(NotificationPersistenceEntity entity) {
        return Notification.reconstitute(entity.getId(), entity.getRecipientId(),
                entity.getReferenceId(), entity.getType(),
                entity.getOccurredAt(), entity.getEmailStatus(), entity.getReadAt());
    }

    public static NotificationPersistenceEntity toPersistence(Notification notification,
                                                             NotificationPersistenceEntity entity) {
        if (notification.getId() == null) {
            entity.setRecipientId(notification.getRecipientId());
            entity.setReferenceId(notification.getReferenceId());
            entity.setType(notification.getType());
            entity.setOccurredAt(notification.getOccurredAt());
        }
        entity.setEmailStatus(notification.getEmailStatus());
        entity.setReadAt(notification.getReadAt());
        return entity;
    }
}

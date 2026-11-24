package com.maquigest.platform.rentals.domain.repositories;

import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationType;
import java.util.Optional;

public interface NotificationRepository {
    Optional<Notification> findById(Long id);
    Optional<Notification> findByRecipientIdAndReferenceIdAndType(NotificationRecipientId recipientId,
            NotificationReferenceId referenceId, NotificationType type);
    Notification save(Notification notification);
}

package com.maquigest.platform.subscriptions.domain.repositories;

import com.maquigest.platform.subscriptions.domain.model.aggregates.Notification;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.NotificationType;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Optional<Notification> findById(Long id);
    Optional<Notification> findByRecipientIdAndFactId(NotificationRecipientId recipientId, java.util.UUID factId);
    List<Notification> findAllByRecipientId(NotificationRecipientId recipientId);
    Optional<Notification> findByIdAndRecipientId(Long id, NotificationRecipientId recipientId);
    Optional<Notification> findByIdAndRecipientIdForUpdate(Long id, NotificationRecipientId recipientId);
    Notification save(Notification notification);
}

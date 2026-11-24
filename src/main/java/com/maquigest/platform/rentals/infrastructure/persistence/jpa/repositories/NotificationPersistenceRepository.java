package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;

import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationReferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository("rentalNotificationPersistenceRepository")
public interface NotificationPersistenceRepository extends JpaRepository<NotificationPersistenceEntity, Long> {
    Optional<NotificationPersistenceEntity> findByRecipientIdAndReferenceIdAndType(NotificationRecipientId recipientId,
            NotificationReferenceId referenceId, NotificationType type);
}

package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;

import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationReferenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;

@Repository("rentalNotificationPersistenceRepository")
public interface NotificationPersistenceRepository extends JpaRepository<NotificationPersistenceEntity, Long> {
    List<NotificationPersistenceEntity> findAllByRecipientIdOrderByOccurredAtDescIdDesc(NotificationRecipientId recipientId);
    Optional<NotificationPersistenceEntity> findByIdAndRecipientId(Long id, NotificationRecipientId recipientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<NotificationPersistenceEntity> findLockedByIdAndRecipientId(Long id, NotificationRecipientId recipientId);

    Optional<NotificationPersistenceEntity> findByRecipientIdAndReferenceIdAndType(NotificationRecipientId recipientId,
            NotificationReferenceId referenceId, NotificationType type);
}

package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers.NotificationPersistenceAssembler;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.NotificationPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository("rentalNotificationRepositoryImpl")
public class NotificationRepositoryImpl implements NotificationRepository {
    private final NotificationPersistenceRepository jpa;
    private final DomainEventPublisher publisher;

    public NotificationRepositoryImpl(NotificationPersistenceRepository jpa, DomainEventPublisher publisher) {
        this.jpa = jpa;
        this.publisher = publisher;
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpa.findById(id).map(NotificationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Notification> findByRecipientIdAndReferenceIdAndType(NotificationRecipientId recipientId,
            NotificationReferenceId referenceId, NotificationType type) {
        return jpa.findByRecipientIdAndReferenceIdAndType(recipientId, referenceId, type)
                .map(NotificationPersistenceAssembler::toDomain);
    }

    @Override
    public Notification save(Notification notification) {
        var entity = notification.getId() == null ? new NotificationPersistenceEntity()
                : jpa.findById(notification.getId()).orElseThrow();
        var saved = jpa.saveAndFlush(NotificationPersistenceAssembler.toPersistence(notification, entity));
        publisher.publishAndClearEvents(notification);
        return NotificationPersistenceAssembler.toDomain(saved);
    }
}

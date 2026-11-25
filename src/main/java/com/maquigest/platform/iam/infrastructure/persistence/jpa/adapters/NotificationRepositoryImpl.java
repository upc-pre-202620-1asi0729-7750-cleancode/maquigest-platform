package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.domain.model.aggregates.Notification;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationReferenceId;
import com.maquigest.platform.iam.domain.model.valueobjects.NotificationType;
import com.maquigest.platform.iam.domain.repositories.NotificationRepository;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers.NotificationPersistenceAssembler;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.NotificationPersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.NotificationPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository("iamNotificationRepositoryImpl")
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
    public Optional<Notification> findByRecipientIdAndFactId(NotificationRecipientId recipientId, java.util.UUID factId) {
        return jpa.findByRecipientIdAndFactId(recipientId, factId.toString())
                .map(NotificationPersistenceAssembler::toDomain);
    }

    @Override
    public List<Notification> findAllByRecipientId(NotificationRecipientId recipientId) {
        return jpa.findAllByRecipientIdOrderByOccurredAtDescIdDesc(recipientId).stream()
                .map(NotificationPersistenceAssembler::toDomain).toList();
    }

    @Override
    public Optional<Notification> findByIdAndRecipientId(Long id, NotificationRecipientId recipientId) {
        return jpa.findByIdAndRecipientId(id, recipientId).map(NotificationPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Notification> findByIdAndRecipientIdForUpdate(Long id, NotificationRecipientId recipientId) {
        return jpa.findLockedByIdAndRecipientId(id, recipientId).map(NotificationPersistenceAssembler::toDomain);
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

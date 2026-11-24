package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("rentalNotificationTransactionalExecutor")
public class NotificationTransactionalExecutor {
    private final NotificationRepository repository;

    public NotificationTransactionalExecutor(NotificationRepository repository) {
        this.repository = repository;
    }

    /** Sequential redelivery of the same business fact returns the existing identity. */
    @Transactional
    public Long handle(CreateNotificationCommand command) {
        return repository.findByRecipientIdAndReferenceIdAndType(command.recipientId(),
                command.referenceId(), command.type())
                .orElseGet(() -> repository.save(new Notification(command))).getId();
    }
}

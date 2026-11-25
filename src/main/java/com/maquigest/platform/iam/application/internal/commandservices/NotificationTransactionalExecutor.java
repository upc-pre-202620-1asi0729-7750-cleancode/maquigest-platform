package com.maquigest.platform.iam.application.internal.commandservices;

import com.maquigest.platform.iam.domain.model.aggregates.Notification;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.iam.domain.model.commands.MarkNotificationAsReadCommand;
import java.time.Instant;
import com.maquigest.platform.iam.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.iam.domain.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("iamNotificationTransactionalExecutor")
public class NotificationTransactionalExecutor {
    private final NotificationRepository repository;

    public NotificationTransactionalExecutor(NotificationRepository repository) {
        this.repository = repository;
    }

    /** Sequential redelivery of the same business fact returns the existing identity. */
    @Transactional
    public Long handle(CreateNotificationCommand command) {
        return repository.findByRecipientIdAndFactId(command.recipientId(), command.factId())
                .orElseGet(() -> repository.save(new Notification(command))).getId();
    }

    @Transactional
    public Result<Notification, ApplicationError> handle(MarkNotificationAsReadCommand command) {
        var found = repository.findByIdAndRecipientIdForUpdate(command.id(), command.recipientId());
        if (found.isEmpty()) return Result.failure(ApplicationError.notFound("Notification", command.id().toString()));
        var notification = found.get();
        if (notification.isRead()) return Result.success(notification);
        var now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        // Allow for a small clock difference between event and read timestamps.
        notification.markAsRead(now.isBefore(notification.getOccurredAt()) ? notification.getOccurredAt() : now);
        return Result.success(repository.save(notification));
    }
}

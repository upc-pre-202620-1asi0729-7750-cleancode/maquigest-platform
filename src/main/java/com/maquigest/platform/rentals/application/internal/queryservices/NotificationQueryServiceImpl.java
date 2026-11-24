package com.maquigest.platform.rentals.application.internal.queryservices;

import com.maquigest.platform.rentals.application.queryservices.NotificationQueryService;
import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.domain.model.queries.GetNotificationsQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetNotificationByIdQuery;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service("rentalNotificationQueryServiceImpl")
@Transactional(readOnly = true)
public class NotificationQueryServiceImpl implements NotificationQueryService {
    private final NotificationRepository repository;

    public NotificationQueryServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Notification> handle(GetNotificationsQuery query) {
        return repository.findAllByRecipientId(query.recipientId());
    }

    @Override
    public Optional<Notification> handle(GetNotificationByIdQuery query) {
        return repository.findByIdAndRecipientId(query.id(), query.recipientId());
    }
}

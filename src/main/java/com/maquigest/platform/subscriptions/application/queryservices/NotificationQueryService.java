package com.maquigest.platform.subscriptions.application.queryservices;

import com.maquigest.platform.subscriptions.domain.model.aggregates.Notification;
import com.maquigest.platform.subscriptions.domain.model.queries.GetNotificationsQuery;
import com.maquigest.platform.subscriptions.domain.model.queries.GetNotificationByIdQuery;
import java.util.List;
import java.util.Optional;

public interface NotificationQueryService {
    List<Notification> handle(GetNotificationsQuery query);
    Optional<Notification> handle(GetNotificationByIdQuery query);
}

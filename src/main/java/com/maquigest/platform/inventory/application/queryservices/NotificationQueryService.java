package com.maquigest.platform.inventory.application.queryservices;

import com.maquigest.platform.inventory.domain.model.aggregates.Notification;
import com.maquigest.platform.inventory.domain.model.queries.GetNotificationsQuery;
import com.maquigest.platform.inventory.domain.model.queries.GetNotificationByIdQuery;
import java.util.List;
import java.util.Optional;

public interface NotificationQueryService {
    List<Notification> handle(GetNotificationsQuery query);
    Optional<Notification> handle(GetNotificationByIdQuery query);
}

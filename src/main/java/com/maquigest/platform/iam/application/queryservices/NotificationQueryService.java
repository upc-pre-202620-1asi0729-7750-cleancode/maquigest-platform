package com.maquigest.platform.iam.application.queryservices;

import com.maquigest.platform.iam.domain.model.aggregates.Notification;
import com.maquigest.platform.iam.domain.model.queries.GetNotificationsQuery;
import com.maquigest.platform.iam.domain.model.queries.GetNotificationByIdQuery;
import java.util.List;
import java.util.Optional;

public interface NotificationQueryService {
    List<Notification> handle(GetNotificationsQuery query);
    Optional<Notification> handle(GetNotificationByIdQuery query);
}

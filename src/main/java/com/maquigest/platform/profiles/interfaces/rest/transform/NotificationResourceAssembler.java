package com.maquigest.platform.profiles.interfaces.rest.transform;

import com.maquigest.platform.profiles.domain.model.aggregates.Notification;
import com.maquigest.platform.profiles.interfaces.rest.resources.NotificationResource;
import java.util.Locale;
import java.util.ResourceBundle;

public final class NotificationResourceAssembler {
    private NotificationResourceAssembler() { }

    public static NotificationResource from(Notification notification, Locale locale) {
        var bundle = ResourceBundle.getBundle("profiles-notifications", locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        var key = "notification." + notification.getType().name().toLowerCase(Locale.ROOT);
        return new NotificationResource(notification.getId(), notification.getReferenceId().referenceId(),
                notification.getType().name(), bundle.getString(key + ".title"), bundle.getString(key + ".message"),
                notification.getOccurredAt(), notification.isRead(), notification.getReadAt(),
                notification.getEmailStatus().name());
    }
}

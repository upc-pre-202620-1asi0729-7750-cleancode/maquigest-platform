package com.maquigest.platform.subscriptions.interfaces.rest.resources;

import java.time.Instant;

/** Public, recipient-scoped representation of a subscriptions notification. */
@io.swagger.v3.oas.annotations.media.Schema(name = "SubscriptionsNotificationResource")
public record NotificationResource(Long id, Long referenceId, String type, String title, String message,
                                   Instant occurredAt, boolean read, Instant readAt, String emailStatus) { }

package com.maquigest.platform.profiles.interfaces.rest.resources;

import java.time.Instant;

/** Public, recipient-scoped representation of a profiles notification. */
@io.swagger.v3.oas.annotations.media.Schema(name = "ProfileNotificationResource")
public record NotificationResource(Long id, Long referenceId, String type, String title, String message,
                                   Instant occurredAt, boolean read, Instant readAt, String emailStatus) { }

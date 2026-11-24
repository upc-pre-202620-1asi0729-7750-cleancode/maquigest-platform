package com.maquigest.platform.maintenance.interfaces.rest.resources;

import java.time.Instant;

/** Public, recipient-scoped representation of a maintenance notification. */
@io.swagger.v3.oas.annotations.media.Schema(name = "MaintenanceNotificationResource")
public record NotificationResource(Long id, Long referenceId, String type, String title, String message,
                                   Instant occurredAt, boolean read, Instant readAt, String emailStatus) { }

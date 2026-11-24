package com.maquigest.platform.rentals.interfaces.rest.resources;

import java.time.Instant;

/** Public, recipient-scoped representation of a rental notification. */
public record NotificationResource(Long id, Long referenceId, String type, String title, String message,
                                   Instant occurredAt, boolean read, Instant readAt, String emailStatus) { }

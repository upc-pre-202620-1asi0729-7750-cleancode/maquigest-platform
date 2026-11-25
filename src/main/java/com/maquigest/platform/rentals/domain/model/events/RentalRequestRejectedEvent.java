package com.maquigest.platform.rentals.domain.model.events;

import java.time.Instant;

/** A rental request business fact owned by Rentals. */
public record RentalRequestRejectedEvent(Long requestId, Long constructionUserId, Instant occurredAt) { }

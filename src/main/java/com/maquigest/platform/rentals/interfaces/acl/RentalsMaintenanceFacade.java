package com.maquigest.platform.rentals.interfaces.acl;

import java.time.Instant;
import java.util.List;

/** Published Rentals read model for Maintenance; does not expose Rental aggregates. */
public interface RentalsMaintenanceFacade {
    record RentalHistory(Long id, Instant occurredAt, String description, String status) { }
    boolean hasActiveRental(Long equipmentId, Long ownerUserId);
    List<RentalHistory> history(Long equipmentId, Long ownerUserId);
}

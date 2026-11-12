package com.maquigest.platform.maintenance.application.ports;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
public interface MaintenanceRentalPort {
    boolean hasActiveRental(Long equipmentId,Long ownerUserId);
    record RentalHistory(Long id,Instant occurredAt,String description,String status) { }
    List<RentalHistory> history(Long equipmentId,Long ownerUserId);
    /** The latest completed rental return, projected across the Rentals ACL. */
    record LatestReturn(Instant returnedAt, boolean maintenanceRequired) { }
    Optional<LatestReturn> latestReturn(Long equipmentId, Long ownerUserId);
}

package com.maquigest.platform.maintenance.application.ports;
import java.time.Instant;
import java.util.List;
public interface MaintenanceRentalPort {
    boolean hasActiveRental(Long equipmentId,Long ownerUserId);
    record RentalHistory(Long id,Instant occurredAt,String description,String status) { }
    List<RentalHistory> history(Long equipmentId,Long ownerUserId);
}

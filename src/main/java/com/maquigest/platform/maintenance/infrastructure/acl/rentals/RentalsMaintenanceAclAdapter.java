package com.maquigest.platform.maintenance.infrastructure.acl.rentals;

import com.maquigest.platform.maintenance.application.ports.MaintenanceRentalPort;
import com.maquigest.platform.rentals.interfaces.acl.RentalsMaintenanceFacade;
import org.springframework.stereotype.Component;
import java.util.List;

/** Anti-corruption adapter: Maintenance does not import Rentals domain or persistence. */
@Component
public class RentalsMaintenanceAclAdapter implements MaintenanceRentalPort {
    private final RentalsMaintenanceFacade rentals;

    public RentalsMaintenanceAclAdapter(RentalsMaintenanceFacade rentals) {
        this.rentals = rentals;
    }

    @Override
    public boolean hasActiveRental(Long equipmentId, Long ownerUserId) {
        return rentals.hasActiveRental(equipmentId, ownerUserId);
    }

    @Override
    public java.util.Optional<LatestReturn> latestReturn(Long equipmentId, Long ownerUserId) {
        return rentals.latestReturn(equipmentId, ownerUserId)
            .map(item -> new LatestReturn(item.returnedAt(), item.maintenanceRequired()));
    }

    @Override
    public List<RentalHistory> history(Long equipmentId, Long ownerUserId) {
        return rentals.history(equipmentId, ownerUserId).stream()
            .map(item -> new RentalHistory(item.id(), item.occurredAt(), item.description(), item.status()))
            .toList();
    }
}

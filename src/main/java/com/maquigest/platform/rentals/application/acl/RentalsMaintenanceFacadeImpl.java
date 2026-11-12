package com.maquigest.platform.rentals.application.acl;

import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import com.maquigest.platform.rentals.domain.repositories.DeliveryRepository;
import com.maquigest.platform.rentals.domain.repositories.EquipmentReturnRepository;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import com.maquigest.platform.rentals.interfaces.acl.RentalsMaintenanceFacade;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Rentals owns rental state and history timestamps; Maintenance only sees immutable projections. */
@Service
public class RentalsMaintenanceFacadeImpl implements RentalsMaintenanceFacade {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final RentalRepository rentals;
    private final DeliveryRepository deliveries;
    private final EquipmentReturnRepository returns;

    public RentalsMaintenanceFacadeImpl(RentalRepository rentals, DeliveryRepository deliveries,
                                        EquipmentReturnRepository returns) {
        this.rentals = rentals;
        this.deliveries = deliveries;
        this.returns = returns;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveRental(Long equipmentId, Long ownerUserId) {
        return rentals.findVisibleTo(ownerUserId).stream()
            .anyMatch(r -> r.getEquipmentId().equals(equipmentId) && r.getStatus() == RentalStatus.ACTIVE);
    }

    /** Read-only Rentals projection: last actual return, not an inferred equipment state. */
    @Override
    @Transactional(readOnly = true)
    public java.util.Optional<LatestReturn> latestReturn(Long equipmentId, Long ownerUserId) {
        return rentals.findVisibleTo(ownerUserId).stream()
            .filter(r -> r.getEquipmentId().equals(equipmentId) && r.getStatus() == RentalStatus.COMPLETED)
            .map(r -> returns.findByRentalId(r.getId())
                .map(item -> new LatestReturn(item.returnedAt(), item.maintenanceRequired())))
            .flatMap(java.util.Optional::stream)
            .max(java.util.Comparator.comparing(LatestReturn::returnedAt));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalHistory> history(Long equipmentId, Long ownerUserId) {
        return rentals.findVisibleTo(ownerUserId).stream().filter(r -> r.getEquipmentId().equals(equipmentId))
            .map(r -> {
                // Completed and active rentals use actual return/delivery timestamps.
                // Confirmed rentals still use the scheduled start date.
                Instant date = r.getPeriod().startDate().atStartOfDay(LIMA).toInstant();
                if (r.getStatus() == RentalStatus.COMPLETED)
                    date = returns.findByRentalId(r.getId()).map(EquipmentReturn::returnedAt)
                        .orElseGet(() -> deliveries.findByRentalId(r.getId()).map(Delivery::deliveredAt)
                            .orElse(r.getPeriod().startDate().atStartOfDay(LIMA).toInstant()));
                else if (r.getStatus() == RentalStatus.ACTIVE)
                    date = deliveries.findByRentalId(r.getId()).map(Delivery::deliveredAt).orElse(date);
                return new RentalHistory(r.getId(), date,
                    "Rental #" + r.getId() + " (" + r.getPeriod().startDate() + " to " + r.getPeriod().endDate() + ")",
                    r.getStatus().name());
            }).toList();
    }
}

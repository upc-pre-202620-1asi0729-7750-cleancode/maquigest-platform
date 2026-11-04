package com.maquigest.platform.maintenance.infrastructure.acl.rentals;
import com.maquigest.platform.maintenance.application.ports.MaintenanceRentalPort;
import com.maquigest.platform.rentals.domain.repositories.RentalRepository;
import com.maquigest.platform.rentals.domain.repositories.DeliveryRepository;
import com.maquigest.platform.rentals.domain.repositories.EquipmentReturnRepository;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import org.springframework.stereotype.Component;
@Component
public class RentalsMaintenanceAclAdapter implements MaintenanceRentalPort {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final RentalRepository rentals;
    private final DeliveryRepository deliveries;
    private final EquipmentReturnRepository returns;
    public RentalsMaintenanceAclAdapter(RentalRepository rentals, DeliveryRepository deliveries,
                                        EquipmentReturnRepository returns){
        this.rentals=rentals;this.deliveries=deliveries;this.returns=returns;
    }
    @Override public boolean hasActiveRental(Long equipmentId,Long owner){
        return rentals.findVisibleTo(owner).stream().anyMatch(r->r.getEquipmentId().equals(equipmentId)&&r.getStatus()==RentalStatus.ACTIVE);
    }
    @Override public List<RentalHistory> history(Long equipmentId,Long owner){
        return rentals.findVisibleTo(owner).stream().filter(r->r.getEquipmentId().equals(equipmentId))
            .map(r->{
                // Completed and active rentals use actual return/delivery timestamps.
                // Confirmed rentals still use the scheduled start date.
                Instant date=r.getPeriod().startDate().atStartOfDay(LIMA).toInstant();
                if(r.getStatus()==RentalStatus.COMPLETED)
                    date=returns.findByRentalId(r.getId()).map(EquipmentReturn::returnedAt)
                        .orElseGet(()->deliveries.findByRentalId(r.getId()).map(Delivery::deliveredAt)
                            .orElse(r.getPeriod().startDate().atStartOfDay(LIMA).toInstant()));
                else if(r.getStatus()==RentalStatus.ACTIVE)
                    date=deliveries.findByRentalId(r.getId()).map(Delivery::deliveredAt).orElse(date);
                return new RentalHistory(r.getId(),date,
                    "Rental #"+r.getId()+" ("+r.getPeriod().startDate()+" to "+r.getPeriod().endDate()+")",r.getStatus().name());
            }).toList();
    }
}

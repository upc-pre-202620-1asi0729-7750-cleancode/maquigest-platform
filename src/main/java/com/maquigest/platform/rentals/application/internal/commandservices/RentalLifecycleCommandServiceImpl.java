package com.maquigest.platform.rentals.application.internal.commandservices;
import com.maquigest.platform.rentals.application.commandservices.RentalLifecycleCommandService;
import com.maquigest.platform.rentals.application.ports.RentalEquipmentPort;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import com.maquigest.platform.rentals.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class RentalLifecycleCommandServiceImpl implements RentalLifecycleCommandService {
    private final RentalRepository rentals;
    private final DeliveryRepository deliveries;
    private final EquipmentReturnRepository returns;
    private final RentalEquipmentPort equipment;
    public RentalLifecycleCommandServiceImpl(RentalRepository rentals,DeliveryRepository deliveries,
            EquipmentReturnRepository returns,RentalEquipmentPort equipment) {
        this.rentals=rentals;this.deliveries=deliveries;this.returns=returns;this.equipment=equipment;
    }
    @Override @Transactional
    public Delivery handle(RegisterDeliveryCommand command) {
        if(command==null || command.rentalId()==null || command.rentalId()<=0 || command.deliveredAt()==null)
            throw RentalsException.invalid("rentalId and deliveredAt are required");
        var rental=rentals.findByIdForUpdate(command.rentalId())
            .orElseThrow(()->RentalsException.notFound("Rental"));
        if(!rental.getRentalCompanyUserId().equals(command.actorUserId())) throw RentalsException.forbidden();
        var previous=deliveries.findByRentalId(rental.getId());
        if(previous.isPresent()) {
            var item=previous.get();
            if(rental.getStatus()==RentalStatus.ACTIVE && item.deliveredAt().equals(command.deliveredAt())
                    && item.notes().equals(command.notes()==null?"":command.notes().trim())) return item;
            throw RentalsException.conflict("Delivery was already registered");
        }
        if(rental.getStatus()!=RentalStatus.CONFIRMED)
            throw RentalsException.conflict("Only CONFIRMED rentals can be delivered");
        equipment.markDelivered(rental.getEquipmentId(),rental.getRentalCompanyUserId(),rental.getAvailabilityBlockId());
        rental.deliver();
        var delivery=deliveries.save(new Delivery(null,rental.getId(),command.deliveredAt(),command.notes()));
        rentals.save(rental);
        return delivery;
    }
    @Override @Transactional
    public EquipmentReturn handle(RegisterReturnCommand command) {
        if(command==null || command.rentalId()==null || command.rentalId()<=0 || command.returnedAt()==null)
            throw RentalsException.invalid("rentalId and returnedAt are required");
        var rental=rentals.findByIdForUpdate(command.rentalId())
            .orElseThrow(()->RentalsException.notFound("Rental"));
        if(!rental.getRentalCompanyUserId().equals(command.actorUserId())) throw RentalsException.forbidden();
        var previous=returns.findByRentalId(rental.getId());
        if(previous.isPresent()) {
            var item=previous.get();
            if(rental.getStatus()==RentalStatus.COMPLETED && item.returnedAt().equals(command.returnedAt())
              && item.maintenanceRequired()==command.maintenanceRequired()
              && item.notes().equals(command.notes()==null?"":command.notes().trim())) return item;
            throw RentalsException.conflict("Return was already registered");
        }
        if(rental.getStatus()!=RentalStatus.ACTIVE)
            throw RentalsException.conflict("Only ACTIVE rentals can be returned");
        var delivery=deliveries.findByRentalId(rental.getId())
            .orElseThrow(()->RentalsException.conflict("A recorded delivery is required before return"));
        if(command.returnedAt().isBefore(delivery.deliveredAt()))
            throw RentalsException.invalid("Return timestamp cannot precede delivery");
        equipment.completeReturn(rental.getEquipmentId(),rental.getRentalCompanyUserId(),
            rental.getAvailabilityBlockId(),command.maintenanceRequired());
        rental.complete();
        var result=returns.save(new EquipmentReturn(null,rental.getId(),command.returnedAt(),command.notes(),command.maintenanceRequired()));
        rentals.save(rental);
        return result;
    }
}

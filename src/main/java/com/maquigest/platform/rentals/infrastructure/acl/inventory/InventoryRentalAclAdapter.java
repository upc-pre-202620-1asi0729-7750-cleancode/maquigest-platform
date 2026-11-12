package com.maquigest.platform.rentals.infrastructure.acl.inventory;

import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFailure;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.ports.RentalEquipmentPort;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import org.springframework.stereotype.Component;

/** Translate Inventory's published language into Rentals' application port. */
@Component
public class InventoryRentalAclAdapter implements RentalEquipmentPort {
    private final InventoryRentalFacade inventory;

    public InventoryRentalAclAdapter(InventoryRentalFacade inventory) {
        this.inventory = inventory;
    }

    private static RentalsException translate(InventoryRentalFailure error) {
        return switch (error.kind()) {
            case INVALID -> RentalsException.invalid(error.getMessage());
            case NOT_FOUND -> RentalsException.notFound("Equipment");
            case FORBIDDEN -> RentalsException.forbidden();
            case CONFLICT -> RentalsException.conflict(error.getMessage());
        };
    }

    @Override
    public EquipmentFacts inspect(Long equipmentId, RentalPeriod period) {
        if (equipmentId == null || equipmentId <= 0 || period == null)
            throw RentalsException.invalid("Invalid equipment or period");
        try {
            var snapshot = inventory.inspect(equipmentId, period.startDate(), period.endDate());
            return new EquipmentFacts(snapshot.id(), snapshot.ownerUserId(), snapshot.availableForPeriod());
        } catch (InventoryRentalFailure error) { throw translate(error); }
    }

    @Override
    public Long reserve(Long equipmentId, Long ownerUserId, RentalPeriod period) {
        if (period == null) throw RentalsException.invalid("Rental period is required");
        try { return inventory.reserve(equipmentId, ownerUserId, period.startDate(), period.endDate()); }
        catch (InventoryRentalFailure error) { throw translate(error); }
    }

    @Override
    public void markDelivered(Long equipmentId, Long ownerUserId, Long blockId) {
        try { inventory.markDelivered(equipmentId, ownerUserId, blockId); }
        catch (InventoryRentalFailure error) { throw translate(error); }
    }

    @Override
    public void completeReturn(Long equipmentId, Long ownerUserId, Long blockId, boolean maintenanceRequired) {
        try { inventory.completeReturn(equipmentId, ownerUserId, blockId, maintenanceRequired); }
        catch (InventoryRentalFailure error) { throw translate(error); }
    }
}

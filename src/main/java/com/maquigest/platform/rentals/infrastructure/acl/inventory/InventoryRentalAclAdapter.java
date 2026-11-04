package com.maquigest.platform.rentals.infrastructure.acl.inventory;
import com.maquigest.platform.rentals.application.ports.RentalEquipmentPort;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import java.time.Instant;
import org.springframework.stereotype.Component;
/** Inventory is only accessed via this ACL; Rentals domain never imports Inventory types. */
@Component
public class InventoryRentalAclAdapter implements RentalEquipmentPort {
    private final EquipmentRepository inventory;
    public InventoryRentalAclAdapter(EquipmentRepository inventory){this.inventory=inventory;}
    private Equipment locked(Long id, Long owner) {
        if(id==null || id<=0 || owner==null || owner<=0) throw RentalsException.invalid("Invalid equipment or owner id");
        var equipment=inventory.findByIdForUpdate(id).orElseThrow(()->RentalsException.notFound("Equipment"));
        if(!equipment.getUserId().equals(owner)) throw RentalsException.forbidden();
        return equipment;
    }
    @Override public EquipmentFacts inspect(Long equipmentId,RentalPeriod period) {
        if(equipmentId==null || equipmentId<=0 || period==null) throw RentalsException.invalid("Invalid equipment or period");
        var equipment=inventory.findById(equipmentId).orElseThrow(()->RentalsException.notFound("Equipment"));
        return new EquipmentFacts(equipment.getId(),equipment.getUserId(),
            equipment.isAvailableFor(period.startDate(),period.endDate()));
    }
    @Override public Long reserve(Long equipmentId,Long ownerUserId,RentalPeriod period) {
        if(period==null) throw RentalsException.invalid("Rental period is required");
        var equipment=locked(equipmentId,ownerUserId);
        try { equipment.reservePeriod(period.startDate(),period.endDate()); }
        catch (IllegalStateException ex) { throw RentalsException.conflict(ex.getMessage()); }
        var saved=inventory.save(equipment);
        return saved.getAllAvailabilityBlocks().stream().filter(AvailabilityBlock::isActive)
            .filter(b->b.getStartDate().equals(period.startDate()) && b.getEndDate().equals(period.endDate()))
            .map(AvailabilityBlock::getId).filter(id->id!=null && id>0).findFirst()
            .orElseThrow(()->new IllegalStateException("The persisted availability block has no id"));
    }
    @Override public void markDelivered(Long equipmentId,Long ownerUserId,Long blockId) {
        var equipment=locked(equipmentId,ownerUserId);
        boolean reserved=equipment.getAvailabilityBlocks().stream().anyMatch(b->blockId.equals(b.getId()));
        if(!reserved) throw RentalsException.conflict("This rental's reservation is not active");
        if(equipment.getStatus()!=EquipmentStatus.AVAILABLE)
            throw RentalsException.conflict("Equipment is not in AVAILABLE status");
        try { equipment.changeOperationalStatus(EquipmentStatus.RENTED); }
        catch(IllegalStateException ex){throw RentalsException.conflict(ex.getMessage());}
        inventory.save(equipment);
    }
    @Override public void completeReturn(Long equipmentId,Long ownerUserId,Long blockId,boolean maintenanceRequired) {
        var equipment=locked(equipmentId,ownerUserId);
        if(equipment.getStatus()!=EquipmentStatus.RENTED)
            throw RentalsException.conflict("Only RENTED equipment can be returned");
        boolean reserved=equipment.getAvailabilityBlocks().stream().anyMatch(b->blockId.equals(b.getId()));
        if(!reserved) throw RentalsException.conflict("This rental's reservation is not active");
        try {
            equipment.changeOperationalStatus(maintenanceRequired?EquipmentStatus.MAINTENANCE:EquipmentStatus.AVAILABLE);
            equipment.releaseAvailabilityBlock(blockId,Instant.now());
        } catch(IllegalStateException ex){throw RentalsException.conflict(ex.getMessage());}
        inventory.save(equipment);
    }
}

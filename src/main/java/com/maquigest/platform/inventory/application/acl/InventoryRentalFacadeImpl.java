package com.maquigest.platform.inventory.application.acl;

import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFailure;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Inventory owns its aggregate invariants, persistence and locking; Rentals sees a snapshot only. */
@Service
public class InventoryRentalFacadeImpl implements InventoryRentalFacade {
    private final EquipmentRepository equipment;

    public InventoryRentalFacadeImpl(EquipmentRepository equipment) {
        this.equipment = equipment;
    }

    private Equipment locked(Long id, Long owner) {
        if (id == null || id <= 0 || owner == null || owner <= 0)
            throw InventoryRentalFailure.invalid("Invalid equipment or owner id");
        var aggregate = equipment.findByIdForUpdate(id)
            .orElseThrow(InventoryRentalFailure::notFound);
        if (!aggregate.getUserId().equals(owner)) throw InventoryRentalFailure.forbidden();
        return aggregate;
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentSnapshot inspect(Long id, LocalDate start, LocalDate end) {
        if (id == null || id <= 0 || start == null || end == null)
            throw InventoryRentalFailure.invalid("Invalid equipment or period");
        var aggregate = equipment.findById(id).orElseThrow(InventoryRentalFailure::notFound);
        return new EquipmentSnapshot(aggregate.getId(), aggregate.getUserId(),
            aggregate.isAvailableFor(start, end));
    }

    /** MANDATORY preserves the Rentals command transaction and the PESSIMISTIC_WRITE lock. */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Long reserve(Long id, Long owner, LocalDate start, LocalDate end) {
        if (start == null || end == null)
            throw InventoryRentalFailure.invalid("Rental period is required");
        var aggregate = locked(id, owner);
        try { aggregate.reservePeriod(start, end); }
        catch (IllegalStateException ex) { throw InventoryRentalFailure.conflict(ex.getMessage()); }
        var saved = equipment.save(aggregate);
        return saved.getAllAvailabilityBlocks().stream().filter(AvailabilityBlock::isActive)
            .filter(block -> block.getStartDate().equals(start) && block.getEndDate().equals(end))
            .map(AvailabilityBlock::getId).filter(blockId -> blockId != null && blockId > 0)
            .findFirst().orElseThrow(() -> new IllegalStateException("The persisted availability block has no id"));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void markDelivered(Long id, Long owner, Long blockId) {
        var aggregate = locked(id, owner);
        boolean reserved = aggregate.getAvailabilityBlocks().stream()
            .anyMatch(block -> blockId.equals(block.getId()));
        if (!reserved) throw InventoryRentalFailure.conflict("This rental's reservation is not active");
        if (aggregate.getStatus() != EquipmentStatus.AVAILABLE)
            throw InventoryRentalFailure.conflict("Equipment is not in AVAILABLE status");
        try { aggregate.changeOperationalStatus(EquipmentStatus.RENTED); }
        catch (IllegalStateException ex) { throw InventoryRentalFailure.conflict(ex.getMessage()); }
        equipment.save(aggregate);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void completeReturn(Long id, Long owner, Long blockId, boolean maintenanceRequired) {
        var aggregate = locked(id, owner);
        if (aggregate.getStatus() != EquipmentStatus.RENTED)
            throw InventoryRentalFailure.conflict("Only RENTED equipment can be returned");
        boolean reserved = aggregate.getAvailabilityBlocks().stream()
            .anyMatch(block -> blockId.equals(block.getId()));
        if (!reserved) throw InventoryRentalFailure.conflict("This rental's reservation is not active");
        try {
            aggregate.changeOperationalStatus(maintenanceRequired ? EquipmentStatus.MAINTENANCE : EquipmentStatus.AVAILABLE);
            aggregate.releaseAvailabilityBlock(blockId, Instant.now());
        } catch (IllegalStateException ex) { throw InventoryRentalFailure.conflict(ex.getMessage()); }
        equipment.save(aggregate);
    }
}

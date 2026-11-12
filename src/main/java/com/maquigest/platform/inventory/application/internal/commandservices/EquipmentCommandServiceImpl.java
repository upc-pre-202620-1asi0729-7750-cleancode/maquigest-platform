package com.maquigest.platform.inventory.application.internal.commandservices;
import com.maquigest.platform.inventory.application.commandservices.EquipmentCommandService;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.*;
import com.maquigest.platform.inventory.domain.repositories.*;
import com.maquigest.platform.shared.application.result.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.NoSuchElementException;

/** The application owns use cases; validation/business failures travel as Result values. */
@Service
public class EquipmentCommandServiceImpl implements EquipmentCommandService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentCategoryRepository categoryRepository;
    public EquipmentCommandServiceImpl(EquipmentRepository equipmentRepository,
        EquipmentCategoryRepository categoryRepository) {
        this.equipmentRepository = equipmentRepository;
        this.categoryRepository = categoryRepository;
    }
    @Override @Transactional
    public Result<Long, ApplicationError> handle(CreateEquipmentCommand command) {
        try {
            var equipment = new Equipment(command);
            if (!categoryRepository.existsById(equipment.getCategoryId())) {
                return Result.failure(ApplicationError.notFound("EquipmentCategory", equipment.getCategoryId().toString()));
            }
            if (equipmentRepository.existsByUserIdAndCodeIgnoreCase(equipment.getUserId(), equipment.getCode())) {
                return Result.failure(ApplicationError.conflict("Equipment", "The owner already uses this code"));
            }
            return Result.success(equipmentRepository.save(equipment).getId());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("equipment", ex.getMessage()));
        }
    }
    @Override @Transactional
    public Result<Equipment, ApplicationError> handle(UpdateEquipmentCommand command) {
        if (command == null || command.id() == null || command.id() <= 0)
            return Result.failure(ApplicationError.validationError("equipmentId", "A positive equipment id is required"));
        var maybeEquipment = equipmentRepository.findById(command.id());
        if (maybeEquipment.isEmpty())
            return Result.failure(ApplicationError.notFound("Equipment", command.id().toString()));
        try {
            var equipment = maybeEquipment.get();
            if (!categoryRepository.existsById(command.categoryId())) {
                return Result.failure(ApplicationError.notFound("EquipmentCategory", String.valueOf(command.categoryId())));
            }
            if (equipmentRepository.existsByUserIdAndCodeIgnoreCaseAndIdNot(equipment.getUserId(), command.code(), command.id())) {
                return Result.failure(ApplicationError.conflict("Equipment", "The owner already uses this code"));
            }
            equipment.updateEditableFields(command);
            return Result.success(equipmentRepository.save(equipment));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("equipment", ex.getMessage()));
        }
    }
    @Override @Transactional
    public Result<Equipment, ApplicationError> handle(ReserveEquipmentPeriodCommand command) {
        if (command == null || command.equipmentId() == null || command.equipmentId() <= 0)
            return Result.failure(ApplicationError.validationError("equipmentId", "A positive equipment id is required"));
        if (command.startDate() == null || command.endDate() == null ||
            command.endDate().isBefore(command.startDate()))
            return Result.failure(ApplicationError.validationError("period", "Date range must be valid and ordered"));
        var equipment = equipmentRepository.findByIdForUpdate(command.equipmentId());
        if (equipment.isEmpty())
            return Result.failure(ApplicationError.notFound("Equipment", String.valueOf(command.equipmentId())));
        try {
            equipment.get().reservePeriod(command.startDate(), command.endDate());
            return Result.success(equipmentRepository.save(equipment.get()));
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.conflict("Equipment", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("period", ex.getMessage()));
        }
    }
    @Override @Transactional
    public Result<Equipment, ApplicationError> handle(ChangeEquipmentOperationalStatusCommand command) {
        if (command == null || command.equipmentId() == null || command.equipmentId() <= 0)
            return Result.failure(ApplicationError.validationError("equipmentId", "A positive equipment id is required"));
        if (command.status() == null)
            return Result.failure(ApplicationError.validationError("status", "Operational status is required"));
        var equipment = equipmentRepository.findByIdForUpdate(command.equipmentId());
        if (equipment.isEmpty())
            return Result.failure(ApplicationError.notFound("Equipment", String.valueOf(command.equipmentId())));
        try {
            equipment.get().changeOperationalStatus(command.status());
            return Result.success(equipmentRepository.save(equipment.get()));
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.conflict("Equipment", ex.getMessage()));
        }
    }
    @Override @Transactional
    public Result<Equipment, ApplicationError> handle(ReleaseEquipmentAvailabilityBlockCommand command) {
        if (command == null || command.equipmentId() == null || command.equipmentId() <= 0 ||
            command.blockId() == null || command.blockId() <= 0)
            return Result.failure(ApplicationError.validationError("reservation", "Positive equipmentId and blockId are required"));
        var equipment = equipmentRepository.findByIdForUpdate(command.equipmentId());
        if (equipment.isEmpty())
            return Result.failure(ApplicationError.notFound("Equipment", String.valueOf(command.equipmentId())));
        try {
            equipment.get().releaseAvailabilityBlock(command.blockId(), Instant.now());
            return Result.success(equipmentRepository.save(equipment.get()));
        } catch (NoSuchElementException ex) {
            return Result.failure(ApplicationError.notFound("AvailabilityBlock", String.valueOf(command.blockId())));
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.conflict("Equipment", ex.getMessage()));
        }
    }

    @Override @Transactional
    public Result<Long, ApplicationError> handle(DeleteEquipmentCommand command) {
        if (command == null || command.equipmentId() == null || command.equipmentId() <= 0)
            return Result.failure(ApplicationError.validationError("equipmentId", "A positive equipment id is required"));
        var maybeEquipment = equipmentRepository.findById(command.equipmentId());
        if (maybeEquipment.isEmpty())
            return Result.failure(ApplicationError.notFound("Equipment", command.equipmentId().toString()));
        if (!maybeEquipment.get().canBeDeleted()) {
            return Result.failure(ApplicationError.businessRuleViolation("Equipment deletion",
                "An equipment with RENTED/MAINTENANCE status or reservation blocks cannot be deleted"));
        }
        equipmentRepository.deleteById(command.equipmentId());
        return Result.success(command.equipmentId());
    }
}

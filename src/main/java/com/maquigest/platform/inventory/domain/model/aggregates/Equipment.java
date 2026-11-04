package com.maquigest.platform.inventory.domain.model.aggregates;

import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.commands.UpdateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Consistency boundary for equipment information, operational status and calendar blocks. */
public final class Equipment extends AbstractDomainAggregateRoot<Equipment> {
    private Long id;
    private final Long userId;
    private String code;
    private String name;
    private String description;
    private Long categoryId;
    private String location;
    private RentalRate rentalRate;
    private EquipmentStatus status;
    private final List<AvailabilityBlock> availabilityBlocks = new ArrayList<>();

    public Equipment(CreateEquipmentCommand command) {
        if (command == null) throw new IllegalArgumentException("CreateEquipmentCommand is required");
        this.userId = positive(command.userId(), "userId");
        updateFields(command.code(), command.name(), command.description(), command.categoryId(),
            command.location(), new RentalRate(command.dailyRate(), command.weeklyRate()));
        this.status = EquipmentStatus.AVAILABLE;
    }

    private Equipment(Long id, CreateEquipmentCommand command) {
        this(command);
        this.id = positive(id, "id");
    }

    public static Equipment reconstitute(Long id, Long userId, String code, String name,
        String description, Long categoryId, String location, RentalRate rentalRate,
        EquipmentStatus status, List<AvailabilityBlock> blocks) {
        var equipment = new Equipment(id, new CreateEquipmentCommand(userId, code, name,
            description, categoryId, location, rentalRate.dailyRate(), rentalRate.weeklyRate()));
        equipment.status = java.util.Objects.requireNonNull(status, "status");
        if (blocks != null) for (var block : blocks) equipment.addAvailabilityBlock(block);
        return equipment;
    }

    private static Long positive(Long value, String field) {
        if (value == null || value <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }
    private static String required(String value, String field, int maxLength) {
        if (value == null || value.isBlank() || value.trim().length() > maxLength) {
            throw new IllegalArgumentException(field + " is required and must not exceed " + maxLength + " characters");
        }
        return value.trim();
    }
    private void updateFields(String code, String name, String description, Long categoryId,
        String location, RentalRate rentalRate) {
        this.code = required(code, "code", 60);
        this.name = required(name, "name", 180);
        this.description = required(description, "description", 3000);
        this.categoryId = positive(categoryId, "categoryId");
        this.location = required(location, "location", 200);
        this.rentalRate = java.util.Objects.requireNonNull(rentalRate, "rentalRate");
    }

    public Equipment updateEditableFields(UpdateEquipmentCommand command) {
        if (command == null || !java.util.Objects.equals(id, command.id())) {
            throw new IllegalArgumentException("Equipment id mismatch");
        }
        updateFields(command.code(), command.name(), command.description(), command.categoryId(),
            command.location(), new RentalRate(command.dailyRate(), command.weeklyRate()));
        return this;
    }
    public boolean isAvailableFor(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);
        return status == EquipmentStatus.AVAILABLE &&
            availabilityBlocks.stream().noneMatch(block -> block.isActive() && block.overlaps(startDate, endDate));
    }
    public void addAvailabilityBlock(AvailabilityBlock block) {
        if (block == null) throw new IllegalArgumentException("block is required");
        if (block.isActive() && availabilityBlocks.stream().anyMatch(existing -> existing.isActive() && existing.overlaps(block.getStartDate(), block.getEndDate()))) {
            throw new IllegalArgumentException("Equipment availability blocks cannot overlap");
        }
        availabilityBlocks.add(block);
    }
    private static void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("Invalid availability date range");
        }
    }
    public boolean canBeDeleted() {
        return status == EquipmentStatus.AVAILABLE && availabilityBlocks.isEmpty();
    }
    public void reservePeriod(LocalDate from, LocalDate to) {
        if (!isAvailableFor(from, to)) {
            throw new IllegalStateException("Equipment is unavailable or the requested period overlaps another reservation");
        }
        addAvailabilityBlock(new AvailabilityBlock(null, from, to));
    }
    /** Release only an individual reservation, under the aggregate's exclusive DB lock. */
    public void releaseAvailabilityBlock(Long blockId, Instant releasedAt) {
        if (blockId == null || blockId <= 0) throw new IllegalArgumentException("blockId must be positive");
        if (status == EquipmentStatus.RENTED) {
            throw new IllegalStateException("Cannot release reservations while equipment is rented");
        }
        var block = availabilityBlocks.stream()
            .filter(item -> blockId.equals(item.getId()))
            .findFirst().orElseThrow(() -> new java.util.NoSuchElementException("Reservation block not found"));
        block.release(releasedAt);
    }

    public void changeOperationalStatus(EquipmentStatus nextStatus) {
        if (nextStatus == null) throw new IllegalArgumentException("status is required");
        if (status == nextStatus) return; // idempotent retry
        boolean allowed = switch (status) {
            case AVAILABLE -> nextStatus == EquipmentStatus.RENTED && !availabilityBlocks.isEmpty()
                || nextStatus == EquipmentStatus.MAINTENANCE;
            case RENTED -> nextStatus == EquipmentStatus.AVAILABLE || nextStatus == EquipmentStatus.MAINTENANCE;
            case MAINTENANCE -> nextStatus == EquipmentStatus.AVAILABLE;
        };
        if (!allowed) throw new IllegalStateException("Equipment operational status transition is not allowed");
        this.status = nextStatus;
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getCategoryId() { return categoryId; }
    public String getLocation() { return location; }
    public RentalRate getRentalRate() { return rentalRate; }
    public EquipmentStatus getStatus() { return status; }
    /** Public availability is only affected by active reservations. */
    public List<AvailabilityBlock> getAvailabilityBlocks() {
        return availabilityBlocks.stream().filter(AvailabilityBlock::isActive).toList();
    }
    /** Infrastructure uses this list to persist the released reservations as history. */
    public List<AvailabilityBlock> getAllAvailabilityBlocks() { return List.copyOf(availabilityBlocks); }
}

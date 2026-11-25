package com.maquigest.platform.rentals.domain.model.aggregates;

import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestApprovedEvent;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestCreatedEvent;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestRejectedEvent;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import com.maquigest.platform.rentals.domain.model.valueobjects.EquipmentId;
import com.maquigest.platform.rentals.domain.model.valueobjects.ConstructionUserId;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalCompanyUserId;
import java.time.Instant;
import java.util.Objects;

public final class RentalRequest extends AbstractDomainAggregateRoot<RentalRequest> {
    private final Long id;
    private final EquipmentId equipmentId;
    private final ConstructionUserId constructionUserId;
    private final RentalCompanyUserId rentalCompanyUserId;
    private RentalPeriod period;
    private final Instant createdAt;
    private RentalRequestStatus status;

    public RentalRequest(Long id, Long equipmentId, Long constructionUserId, Long rentalCompanyUserId,
                         RentalPeriod period, RentalRequestStatus status, Instant createdAt) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id = id;
        this.equipmentId = new EquipmentId(equipmentId);
        this.constructionUserId = new ConstructionUserId(constructionUserId);
        this.rentalCompanyUserId = new RentalCompanyUserId(rentalCompanyUserId);
        if (this.constructionUserId.value().equals(this.rentalCompanyUserId.value()))
            throw new IllegalArgumentException("Participants must differ");
        this.period = Objects.requireNonNull(period, "period");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    /** A client may change the requested dates only while the request is PENDING. */
    public void reschedule(RentalPeriod newPeriod) {
        requirePending();
        this.period = Objects.requireNonNull(newPeriod, "newPeriod");
    }

    public void approve() {
        requirePending();
        status = RentalRequestStatus.APPROVED;
        registerDomainEvent(new RentalRequestApprovedEvent(id, equipmentId.value(), constructionUserId.value(),
            rentalCompanyUserId.value(), period.startDate(), period.endDate()));
    }

    /** Registers creation only after the persistence identity has been assigned. */
    public void onCreated() {
        if (id == null) throw new IllegalStateException("Request must be persisted before creation event");
        requirePending();
        registerDomainEvent(new RentalRequestCreatedEvent(id, rentalCompanyUserId.value(), createdAt));
    }

    public void reject() {
        requirePending();
        status = RentalRequestStatus.REJECTED;
        registerDomainEvent(new RentalRequestRejectedEvent(id, constructionUserId.value(), Instant.now()));
    }

    private void requirePending() {
        if (status != RentalRequestStatus.PENDING)
            throw new IllegalStateException("Only PENDING requests can change status");
    }

    public Long getId() { return id; }
    public Long getEquipmentId() { return equipmentId.value(); }
    public Long getConstructionUserId() { return constructionUserId.value(); }
    public Long getRentalCompanyUserId() { return rentalCompanyUserId.value(); }
    public RentalPeriod getPeriod() { return period; }
    public RentalRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}

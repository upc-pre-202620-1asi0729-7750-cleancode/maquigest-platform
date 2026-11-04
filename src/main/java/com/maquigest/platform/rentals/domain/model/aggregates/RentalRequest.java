package com.maquigest.platform.rentals.domain.model.aggregates;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.model.events.RentalRequestApprovedEvent;
import java.time.Instant;
import java.util.Objects;
public final class RentalRequest extends AbstractDomainAggregateRoot<RentalRequest> {
    private final Long id, equipmentId, constructionUserId, rentalCompanyUserId;
    private final RentalPeriod period;
    private final Instant createdAt;
    private RentalRequestStatus status;
    public RentalRequest(Long id, Long equipmentId, Long constructionUserId, Long rentalCompanyUserId,
                         RentalPeriod period, RentalRequestStatus status, Instant createdAt) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id = id;
        this.equipmentId = positive(equipmentId, "equipmentId");
        this.constructionUserId = positive(constructionUserId, "constructionUserId");
        this.rentalCompanyUserId = positive(rentalCompanyUserId, "rentalCompanyUserId");
        if (constructionUserId.equals(rentalCompanyUserId)) throw new IllegalArgumentException("Participants must differ");
        this.period = Objects.requireNonNull(period, "period");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }
    private static Long positive(Long value, String field) {
        if (value == null || value <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }
    public void approve() {
        requirePending(); status = RentalRequestStatus.APPROVED;
        registerDomainEvent(new RentalRequestApprovedEvent(id, equipmentId, constructionUserId,
            rentalCompanyUserId, period.startDate(), period.endDate()));
    }
    public void reject() { requirePending(); status = RentalRequestStatus.REJECTED; }
    private void requirePending() {
        if (status != RentalRequestStatus.PENDING) throw new IllegalStateException("Only PENDING requests can change status");
    }
    public Long getId() { return id; }
    public Long getEquipmentId() { return equipmentId; }
    public Long getConstructionUserId() { return constructionUserId; }
    public Long getRentalCompanyUserId() { return rentalCompanyUserId; }
    public RentalPeriod getPeriod() { return period; }
    public RentalRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}

package com.maquigest.platform.rentals.domain.model.aggregates;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.model.events.*;
import java.util.Objects;
public final class Rental extends AbstractDomainAggregateRoot<Rental> {
    private final Long id, requestId, equipmentId, constructionUserId, rentalCompanyUserId, availabilityBlockId;
    private final RentalPeriod period;
    private RentalStatus status;
    public Rental(Long id, Long requestId, Long equipmentId, Long constructionUserId, Long rentalCompanyUserId,
                  Long availabilityBlockId, RentalPeriod period, RentalStatus status) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id=id;
        this.requestId=positive(requestId,"requestId");
        this.equipmentId=positive(equipmentId,"equipmentId");
        this.constructionUserId=positive(constructionUserId,"constructionUserId");
        this.rentalCompanyUserId=positive(rentalCompanyUserId,"rentalCompanyUserId");
        this.availabilityBlockId=positive(availabilityBlockId,"availabilityBlockId");
        this.period=Objects.requireNonNull(period,"period");
        this.status=Objects.requireNonNull(status,"status");
    }
    private static Long positive(Long value,String field) {
        if(value==null || value<=0) throw new IllegalArgumentException(field+" must be positive");
        return value;
    }
    public void deliver() {
        if(status!=RentalStatus.CONFIRMED) throw new IllegalStateException("Only CONFIRMED rentals can be delivered");
        status=RentalStatus.ACTIVE;
        registerDomainEvent(new RentalActivatedEvent(id,equipmentId));
    }
    public void complete() {
        if(status!=RentalStatus.ACTIVE) throw new IllegalStateException("Only ACTIVE rentals can be returned");
        status=RentalStatus.COMPLETED;
        registerDomainEvent(new RentalCompletedEvent(id,equipmentId));
    }
    public Long getId(){return id;}
    public Long getRequestId(){return requestId;}
    public Long getEquipmentId(){return equipmentId;}
    public Long getConstructionUserId(){return constructionUserId;}
    public Long getRentalCompanyUserId(){return rentalCompanyUserId;}
    public Long getAvailabilityBlockId(){return availabilityBlockId;}
    public RentalPeriod getPeriod(){return period;}
    public RentalStatus getStatus(){return status;}
}

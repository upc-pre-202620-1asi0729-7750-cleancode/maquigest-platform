package com.maquigest.platform.rentals.domain.model.aggregates;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.model.events.*;
import java.util.Objects;
public final class Rental extends AbstractDomainAggregateRoot<Rental> {
    private final Long id, requestId;
    private final EquipmentId equipmentId;
    private final ConstructionUserId constructionUserId;
    private final RentalCompanyUserId rentalCompanyUserId;
    private final AvailabilityBlockId availabilityBlockId;
    private final RentalPeriod period;
    private RentalStatus status;
    public Rental(Long id, Long requestId, Long equipmentId, Long constructionUserId, Long rentalCompanyUserId,
                  Long availabilityBlockId, RentalPeriod period, RentalStatus status) {
        if (id != null && id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id=id;
        this.requestId=positive(requestId,"requestId");
        this.equipmentId=new EquipmentId(equipmentId);
        this.constructionUserId=new ConstructionUserId(constructionUserId);
        this.rentalCompanyUserId=new RentalCompanyUserId(rentalCompanyUserId);
        this.availabilityBlockId=new AvailabilityBlockId(availabilityBlockId);
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
        registerDomainEvent(new RentalActivatedEvent(id,equipmentId.value()));
    }
    public void complete() {
        if(status!=RentalStatus.ACTIVE) throw new IllegalStateException("Only ACTIVE rentals can be returned");
        status=RentalStatus.COMPLETED;
        registerDomainEvent(new RentalCompletedEvent(id,equipmentId.value()));
    }
    public Long getId(){return id;}
    public Long getRequestId(){return requestId;}
    public Long getEquipmentId(){return equipmentId.value();}
    public Long getConstructionUserId(){return constructionUserId.value();}
    public Long getRentalCompanyUserId(){return rentalCompanyUserId.value();}
    public Long getAvailabilityBlockId(){return availabilityBlockId.value();}
    public RentalPeriod getPeriod(){return period;}
    public RentalStatus getStatus(){return status;}
}

package com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalPersistenceEntity;
public final class RentalPersistenceAssembler {
    private RentalPersistenceAssembler() { }
    public static Rental toDomain(RentalPersistenceEntity e) {
        return new Rental(e.getId(),e.getRequestId(),e.getEquipmentId(),e.getConstructionUserId(),e.getRentalCompanyUserId(),
          e.getAvailabilityBlockId(),new RentalPeriod(e.getStartDate(),e.getEndDate()),e.getStatus());
    }
    public static RentalPersistenceEntity toNewPersistence(Rental r) {
        var e=new RentalPersistenceEntity();
        e.setRequestId(r.getRequestId());e.setEquipmentId(r.getEquipmentId());e.setConstructionUserId(r.getConstructionUserId());
        e.setRentalCompanyUserId(r.getRentalCompanyUserId());e.setAvailabilityBlockId(r.getAvailabilityBlockId());
        e.setStartDate(r.getPeriod().startDate());e.setEndDate(r.getPeriod().endDate());e.setStatus(r.getStatus());return e;
    }
}

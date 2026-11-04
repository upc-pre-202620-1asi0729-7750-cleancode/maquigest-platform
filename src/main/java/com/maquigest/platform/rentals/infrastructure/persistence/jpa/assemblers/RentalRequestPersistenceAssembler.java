package com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalRequestPersistenceEntity;
public final class RentalRequestPersistenceAssembler {
    private RentalRequestPersistenceAssembler() { }
    public static RentalRequest toDomain(RentalRequestPersistenceEntity e) {
        return new RentalRequest(e.getId(),e.getEquipmentId(),e.getConstructionUserId(),e.getRentalCompanyUserId(),
          new RentalPeriod(e.getStartDate(),e.getEndDate()),e.getStatus(),e.getCreatedAt());
    }
    public static RentalRequestPersistenceEntity toNewPersistence(RentalRequest r) {
        var e=new RentalRequestPersistenceEntity();
        e.setEquipmentId(r.getEquipmentId()); e.setConstructionUserId(r.getConstructionUserId());
        e.setRentalCompanyUserId(r.getRentalCompanyUserId());
        e.setStartDate(r.getPeriod().startDate());e.setEndDate(r.getPeriod().endDate());
        e.setStatus(r.getStatus());e.setCreatedAt(r.getCreatedAt());return e;
    }
}

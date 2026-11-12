package com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.EquipmentReturnPersistenceEntity;
public final class EquipmentReturnPersistenceAssembler {
    private EquipmentReturnPersistenceAssembler(){}
    public static EquipmentReturn toDomain(EquipmentReturnPersistenceEntity e){return new EquipmentReturn(e.getId(),e.getRentalId(),e.getReturnedAt(),e.getNotes(),e.isMaintenanceRequired());}
    public static EquipmentReturnPersistenceEntity toNewPersistence(EquipmentReturn r){var e=new EquipmentReturnPersistenceEntity();e.setRentalId(r.rentalId());e.setReturnedAt(r.returnedAt());e.setNotes(r.notes());e.setMaintenanceRequired(r.maintenanceRequired());return e;}
}

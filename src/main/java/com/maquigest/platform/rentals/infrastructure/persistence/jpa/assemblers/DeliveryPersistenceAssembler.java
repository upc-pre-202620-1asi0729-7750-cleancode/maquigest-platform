package com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.DeliveryPersistenceEntity;
public final class DeliveryPersistenceAssembler {
    private DeliveryPersistenceAssembler(){}
    public static Delivery toDomain(DeliveryPersistenceEntity e){return new Delivery(e.getId(),e.getRentalId(),e.getDeliveredAt(),e.getNotes());}
    public static DeliveryPersistenceEntity toNewPersistence(Delivery d){var e=new DeliveryPersistenceEntity();e.setRentalId(d.rentalId());e.setDeliveredAt(d.deliveredAt());e.setNotes(d.notes());return e;}
}

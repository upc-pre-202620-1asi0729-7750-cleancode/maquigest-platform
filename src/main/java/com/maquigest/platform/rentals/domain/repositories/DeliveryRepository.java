package com.maquigest.platform.rentals.domain.repositories;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import java.util.*;
public interface DeliveryRepository {
    Optional<Delivery> findByRentalId(Long rentalId);
    List<Delivery> findByRentalIds(Collection<Long> rentalIds);
    Delivery save(Delivery delivery);
}

package com.maquigest.platform.rentals.domain.repositories;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import java.util.*;
public interface EquipmentReturnRepository {
    Optional<EquipmentReturn> findByRentalId(Long rentalId);
    List<EquipmentReturn> findByRentalIds(Collection<Long> rentalIds);
    EquipmentReturn save(EquipmentReturn equipmentReturn);
}

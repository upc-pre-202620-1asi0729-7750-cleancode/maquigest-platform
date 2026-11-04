package com.maquigest.platform.inventory.application.queryservices;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.queries.*;
import java.util.List;
import java.util.Optional;
public interface EquipmentQueryService {
    Optional<Equipment> handle(GetEquipmentByIdQuery query);
    List<Equipment> handle(GetAllEquipmentQuery query);
    Optional<Boolean> handle(CheckEquipmentAvailabilityQuery query);
}

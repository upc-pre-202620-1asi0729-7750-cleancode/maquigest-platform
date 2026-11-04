package com.maquigest.platform.inventory.application.commandservices;
import com.maquigest.platform.inventory.domain.model.commands.*;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.shared.application.result.*;
public interface EquipmentCommandService {
    Result<Long, ApplicationError> handle(CreateEquipmentCommand command);
    Result<Equipment, ApplicationError> handle(UpdateEquipmentCommand command);
    Result<Long, ApplicationError> handle(DeleteEquipmentCommand command);
}

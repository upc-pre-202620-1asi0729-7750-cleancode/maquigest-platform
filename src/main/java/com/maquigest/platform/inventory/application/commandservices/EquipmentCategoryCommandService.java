package com.maquigest.platform.inventory.application.commandservices;
import com.maquigest.platform.inventory.domain.model.commands.SeedEquipmentCategoriesCommand;
import com.maquigest.platform.shared.application.result.*;
public interface EquipmentCategoryCommandService {
    Result<Integer, ApplicationError> handle(SeedEquipmentCategoriesCommand command);
}

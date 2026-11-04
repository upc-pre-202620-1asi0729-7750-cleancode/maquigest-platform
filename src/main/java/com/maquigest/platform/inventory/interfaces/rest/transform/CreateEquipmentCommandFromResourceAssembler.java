package com.maquigest.platform.inventory.interfaces.rest.transform;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.interfaces.rest.resources.CreateEquipmentResource;
public final class CreateEquipmentCommandFromResourceAssembler {
    private CreateEquipmentCommandFromResourceAssembler() { }
    public static CreateEquipmentCommand toCommandFromResource(CreateEquipmentResource r) {
        // status, id and availabilityBlocks are NOT accepted as input to inventory creation.
        return new CreateEquipmentCommand(r.userId(),r.code(),r.name(),r.description(),
            r.categoryId(),r.location(),r.dailyRate(),r.weeklyRate());
    }
}

package com.maquigest.platform.inventory.interfaces.rest.transform;
import com.maquigest.platform.inventory.domain.model.commands.UpdateEquipmentCommand;
import com.maquigest.platform.inventory.interfaces.rest.resources.UpdateEquipmentResource;
public final class UpdateEquipmentCommandFromResourceAssembler {
    private UpdateEquipmentCommandFromResourceAssembler() { }
    public static UpdateEquipmentCommand toCommandFromResource(Long id, UpdateEquipmentResource r) {
        return new UpdateEquipmentCommand(id,r.code(),r.name(),r.description(),r.categoryId(),
            r.location(),r.dailyRate(),r.weeklyRate());
    }
}

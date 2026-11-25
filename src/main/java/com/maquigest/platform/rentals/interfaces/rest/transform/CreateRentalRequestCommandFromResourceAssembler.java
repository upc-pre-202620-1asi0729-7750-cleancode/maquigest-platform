package com.maquigest.platform.rentals.interfaces.rest.transform;

import com.maquigest.platform.rentals.domain.model.commands.CreateRentalRequestCommand;
import com.maquigest.platform.rentals.interfaces.rest.resources.CreateRentalRequestResource;

public final class CreateRentalRequestCommandFromResourceAssembler {
    private CreateRentalRequestCommandFromResourceAssembler() { }

    public static CreateRentalRequestCommand toCommandFromResource(CreateRentalRequestResource resource, Long actorUserId) {
        return new CreateRentalRequestCommand(resource.equipmentId(), actorUserId,
            resource.rentalCompanyUserId(), RentalResourceAssembler.parsePeriod(resource.startDate(), resource.endDate()));
    }
}

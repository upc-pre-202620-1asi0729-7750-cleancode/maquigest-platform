package com.maquigest.platform.rentals.interfaces.rest.transform;

import com.maquigest.platform.rentals.domain.model.commands.RescheduleRentalRequestCommand;
import com.maquigest.platform.rentals.interfaces.rest.resources.RescheduleRentalRequestResource;

public final class RescheduleRentalRequestCommandFromResourceAssembler {
    private RescheduleRentalRequestCommandFromResourceAssembler() { }

    public static RescheduleRentalRequestCommand toCommandFromResource(Long requestId, Long actorUserId, RescheduleRentalRequestResource resource) {
        return new RescheduleRentalRequestCommand(requestId, actorUserId, RentalResourceAssembler.parsePeriod(resource.startDate(), resource.endDate()));
    }
}

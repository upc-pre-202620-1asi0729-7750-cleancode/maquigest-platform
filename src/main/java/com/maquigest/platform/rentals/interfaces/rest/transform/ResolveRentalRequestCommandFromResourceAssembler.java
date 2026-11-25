package com.maquigest.platform.rentals.interfaces.rest.transform;

import com.maquigest.platform.rentals.domain.model.commands.ResolveRentalRequestCommand;
import com.maquigest.platform.rentals.interfaces.rest.resources.UpdateRentalRequestResource;

public final class ResolveRentalRequestCommandFromResourceAssembler {
    private ResolveRentalRequestCommandFromResourceAssembler() { }

    public static ResolveRentalRequestCommand toCommandFromResource(Long requestId, Long actorUserId, UpdateRentalRequestResource resource) {
        return new ResolveRentalRequestCommand(requestId, actorUserId, resource.status());
    }
}

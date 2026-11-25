package com.maquigest.platform.rentals.interfaces.rest.transform;

import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.interfaces.rest.resources.RentalRequestResource;

public final class RentalRequestResourceFromEntityAssembler {
    private RentalRequestResourceFromEntityAssembler() { }

    public static RentalRequestResource toResourceFromEntity(RentalRequest request) {
        return new RentalRequestResource(request.getId(), request.getEquipmentId(), request.getConstructionUserId(),
            request.getRentalCompanyUserId(), RentalResourceAssembler.periodStart(request.getPeriod()),
            RentalResourceAssembler.periodEnd(request.getPeriod()), request.getStatus(), request.getCreatedAt());
    }
}

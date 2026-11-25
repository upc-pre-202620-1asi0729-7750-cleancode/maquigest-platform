package com.maquigest.platform.rentals.interfaces.rest.transform;

import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
import com.maquigest.platform.rentals.interfaces.rest.resources.RentalLocationResource;
public final class RentalLocationResourceAssembler {
    private RentalLocationResourceAssembler() { }
    public static RentalLocationResource from(GeographicLocation location) {
        return new RentalLocationResource(location.formattedAddress(), location.latitude(), location.longitude(), location.placeId());
    }
}

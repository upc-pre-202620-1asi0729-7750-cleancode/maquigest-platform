package com.maquigest.platform.rentals.application.ports;

import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
public interface RentalGeocodingPort {
    GeographicLocation locate(String address, String language);
}

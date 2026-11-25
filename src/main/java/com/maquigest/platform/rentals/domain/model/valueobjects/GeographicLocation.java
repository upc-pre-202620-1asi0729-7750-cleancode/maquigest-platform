package com.maquigest.platform.rentals.domain.model.valueobjects;

public record GeographicLocation(String formattedAddress, double latitude, double longitude, String placeId) {
    public GeographicLocation {
        if (formattedAddress == null || formattedAddress.isBlank() || !Double.isFinite(latitude)
                || !Double.isFinite(longitude) || Math.abs(latitude) > 90 || Math.abs(longitude) > 180
                || placeId == null || placeId.isBlank()) throw new IllegalArgumentException("Invalid geographic location");
    }
}

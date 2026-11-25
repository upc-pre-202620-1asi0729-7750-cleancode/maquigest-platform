package com.maquigest.platform.rentals.interfaces.rest.resources;

public record RentalLocationResource(String formattedAddress, double latitude, double longitude, String placeId) { }

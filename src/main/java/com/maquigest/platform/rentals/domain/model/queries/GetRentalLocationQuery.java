package com.maquigest.platform.rentals.domain.model.queries;

public record GetRentalLocationQuery(Long rentalId, Long actorUserId, String address, String language) { }

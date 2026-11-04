package com.maquigest.platform.rentals.interfaces.rest.resources;

import java.time.Instant;

public record RescheduleRentalRequestResource(Instant startDate, Instant endDate) { }

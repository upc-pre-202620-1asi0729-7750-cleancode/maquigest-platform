package com.maquigest.platform.rentals.interfaces.rest.resources;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import java.time.Instant;
public record RentalResource(Long id,Long equipmentId,Long constructionUserId,Long rentalCompanyUserId,
    Instant startDate,Instant endDate,RentalStatus status) { }

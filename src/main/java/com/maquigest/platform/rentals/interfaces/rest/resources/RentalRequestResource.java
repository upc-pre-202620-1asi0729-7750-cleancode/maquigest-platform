package com.maquigest.platform.rentals.interfaces.rest.resources;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import java.time.Instant;
public record RentalRequestResource(Long id,Long equipmentId,Long constructionUserId,Long rentalCompanyUserId,
    Instant startDate,Instant endDate,RentalRequestStatus status,Instant createdAt) { }

package com.maquigest.platform.rentals.interfaces.rest.resources;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import java.time.Instant;
/** Creation payload. Legacy id and createdAt are accepted but never used to create the aggregate. */
public record CreateRentalRequestResource(Long id,Long equipmentId,Long constructionUserId,Long rentalCompanyUserId,
    Instant startDate,Instant endDate,RentalRequestStatus status,Instant createdAt) { }

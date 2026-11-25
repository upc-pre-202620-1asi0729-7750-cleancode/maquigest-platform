package com.maquigest.platform.rentals.interfaces.rest.resources;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import java.time.Instant;
/** Decision payload. Identity and period are checked against the existing request; createdAt is ignored. */
public record UpdateRentalRequestResource(Long id,Long equipmentId,Long constructionUserId,Long rentalCompanyUserId,
    Instant startDate,Instant endDate,RentalRequestStatus status,Instant createdAt) { }

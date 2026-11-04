package com.maquigest.platform.rentals.domain.model.events;
public record RentalRequestApprovedEvent(Long requestId, Long equipmentId, Long constructionUserId, Long rentalCompanyUserId, java.time.LocalDate startDate, java.time.LocalDate endDate) { }

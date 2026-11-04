package com.maquigest.platform.rentals.domain.model.commands;
public record CreateRentalRequestCommand(Long equipmentId, Long constructionUserId, Long rentalCompanyUserId, com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod period) { }

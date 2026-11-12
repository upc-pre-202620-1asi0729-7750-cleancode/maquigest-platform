package com.maquigest.platform.rentals.application.commandservices;

import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.commands.CreateRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.commands.ResolveRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.commands.RescheduleRentalRequestCommand;

import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.shared.application.result.ApplicationError;
public interface RentalRequestCommandService {
    Result<RentalRequest, ApplicationError> handle(CreateRentalRequestCommand command);
    Result<RentalRequest, ApplicationError> handle(ResolveRentalRequestCommand command);
    Result<RentalRequest, ApplicationError> handle(RescheduleRentalRequestCommand command);
}

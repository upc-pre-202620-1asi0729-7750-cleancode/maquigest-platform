package com.maquigest.platform.rentals.application.commandservices;

import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.commands.CreateRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.commands.ResolveRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.commands.RescheduleRentalRequestCommand;

public interface RentalRequestCommandService {
    RentalRequest handle(CreateRentalRequestCommand command);
    RentalRequest handle(ResolveRentalRequestCommand command);
    RentalRequest handle(RescheduleRentalRequestCommand command);
}

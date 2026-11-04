package com.maquigest.platform.rentals.application.commandservices;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
public interface RentalRequestCommandService {
    RentalRequest handle(CreateRentalRequestCommand command);
    RentalRequest handle(ResolveRentalRequestCommand command);
}

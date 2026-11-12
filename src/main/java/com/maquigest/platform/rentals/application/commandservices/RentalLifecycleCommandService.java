package com.maquigest.platform.rentals.application.commandservices;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.shared.application.result.ApplicationError;
public interface RentalLifecycleCommandService {
    Result<Delivery, ApplicationError> handle(RegisterDeliveryCommand command);
    Result<EquipmentReturn, ApplicationError> handle(RegisterReturnCommand command);
}

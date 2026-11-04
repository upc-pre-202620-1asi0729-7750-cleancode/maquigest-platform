package com.maquigest.platform.rentals.application.commandservices;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
public interface RentalLifecycleCommandService {
    Delivery handle(RegisterDeliveryCommand command);
    EquipmentReturn handle(RegisterReturnCommand command);
}

package com.maquigest.platform.rentals.application.queryservices;
import com.maquigest.platform.rentals.domain.model.aggregates.*;
import com.maquigest.platform.rentals.domain.model.queries.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
import java.util.*;
public interface RentalQueryService {
    List<RentalRequest> handle(GetRentalRequestsQuery query);
    List<Rental> handle(GetRentalsQuery query);
    Optional<RentalRequest> handle(GetRentalRequestByIdQuery query);
    Optional<Rental> handle(GetRentalByIdQuery query);
    List<Delivery> handle(GetDeliveriesQuery query);
    List<EquipmentReturn> handle(GetEquipmentReturnsQuery query);
}

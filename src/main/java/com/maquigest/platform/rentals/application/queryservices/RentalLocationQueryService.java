package com.maquigest.platform.rentals.application.queryservices;

import com.maquigest.platform.rentals.domain.model.queries.GetRentalLocationQuery;
import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
public interface RentalLocationQueryService {
    GeographicLocation handle(GetRentalLocationQuery query);
}

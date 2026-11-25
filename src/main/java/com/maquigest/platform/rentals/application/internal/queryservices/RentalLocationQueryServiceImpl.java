package com.maquigest.platform.rentals.application.internal.queryservices;

import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import static com.maquigest.platform.rentals.application.exceptions.GeographicLookupException.Code.*;
import com.maquigest.platform.rentals.application.ports.RentalGeocodingPort;
import com.maquigest.platform.rentals.application.queryservices.RentalLocationQueryService;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalByIdQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalLocationQuery;
import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
import org.springframework.stereotype.Service;

@Service
public class RentalLocationQueryServiceImpl implements RentalLocationQueryService {
    private final RentalQueryService rentals;
    private final RentalGeocodingPort geocoding;
    public RentalLocationQueryServiceImpl(RentalQueryService rentals, RentalGeocodingPort geocoding) {
        this.rentals = rentals; this.geocoding = geocoding;
    }
    public GeographicLocation handle(GetRentalLocationQuery query) {
        if (query == null || query.rentalId() == null || query.rentalId() <= 0 || query.actorUserId() == null
                || query.actorUserId() <= 0 || query.address() == null || query.address().strip().length() < 3
                || query.address().length() > 250 || query.address().chars().anyMatch(Character::isISOControl))
            throw new GeographicLookupException(INVALID);
        var rental = rentals.handle(new GetRentalByIdQuery(query.rentalId(), query.actorUserId()))
                .orElseThrow(() -> new GeographicLookupException(NOT_FOUND));
        if (!query.actorUserId().equals(rental.getRentalCompanyUserId()))
            throw new GeographicLookupException(FORBIDDEN);
        // The database read has completed before the external call. No lifecycle transaction is modified.
        return geocoding.locate(query.address().strip(), "es".equals(query.language()) ? "es" : "en");
    }
}

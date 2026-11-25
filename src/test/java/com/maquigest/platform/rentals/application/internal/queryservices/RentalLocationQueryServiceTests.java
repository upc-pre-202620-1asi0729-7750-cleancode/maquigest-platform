package com.maquigest.platform.rentals.application.internal.queryservices;

import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import static com.maquigest.platform.rentals.application.exceptions.GeographicLookupException.Code.*;
import com.maquigest.platform.rentals.application.ports.RentalGeocodingPort;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.queries.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalLocationQueryServiceTests {
    private final RentalQueryService rentals = mock(RentalQueryService.class);
    private final RentalGeocodingPort maps = mock(RentalGeocodingPort.class);
    private final RentalLocationQueryServiceImpl service = new RentalLocationQueryServiceImpl(rentals, maps);
    private final GetRentalLocationQuery query = new GetRentalLocationQuery(1L, 2L, " Lima, Peru ", "es");
    @Test void ownerCanLookupWithoutChangingRental() {
        var rental = mock(Rental.class); when(rental.getRentalCompanyUserId()).thenReturn(2L);
        when(rentals.handle(new GetRentalByIdQuery(1L, 2L))).thenReturn(Optional.of(rental));
        var expected = new GeographicLocation("Lima, Peru", -12, -77, "place"); when(maps.locate("Lima, Peru", "es")).thenReturn(expected);
        assertEquals(expected, service.handle(query)); verify(rental).getRentalCompanyUserId(); verifyNoMoreInteractions(rental);
    }
    @Test void invisibleRentalNeverCallsProvider() {
        when(rentals.handle(new GetRentalByIdQuery(1L, 2L))).thenReturn(Optional.empty());
        assertEquals(NOT_FOUND, assertThrows(GeographicLookupException.class, () -> service.handle(query)).code()); verifyNoInteractions(maps);
    }
    @Test void constructionParticipantCannotUseOwnerLookup() {
        var rental = mock(Rental.class); when(rental.getRentalCompanyUserId()).thenReturn(3L);
        when(rentals.handle(new GetRentalByIdQuery(1L, 2L))).thenReturn(Optional.of(rental));
        assertEquals(FORBIDDEN, assertThrows(GeographicLookupException.class, () -> service.handle(query)).code()); verifyNoInteractions(maps);
    }
    @Test void invalidAddressNeverCallsProvider() {
        assertEquals(INVALID, assertThrows(GeographicLookupException.class, () -> service.handle(new GetRentalLocationQuery(1L, 2L, " ", "es"))).code()); verifyNoInteractions(rentals, maps);
    }
}

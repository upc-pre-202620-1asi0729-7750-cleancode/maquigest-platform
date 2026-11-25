package com.maquigest.platform.rentals.interfaces.rest;

import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import static com.maquigest.platform.rentals.application.exceptions.GeographicLookupException.Code.*;
import com.maquigest.platform.rentals.application.queryservices.RentalLocationQueryService;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalLocationQuery;
import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import java.util.Locale;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalLocationControllerTests {
    private final RentalLocationQueryService service = mock(RentalLocationQueryService.class);
    private final RentalActorPolicy actors = mock(RentalActorPolicy.class);
    private final Authentication auth = mock(Authentication.class);
    private final RentalLocationController controller = new RentalLocationController(service, actors);
    @Test void ownerResourceIsNotCacheable() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es-PE"));
        try {
            when(actors.current(auth)).thenReturn(Optional.of(new RentalActorPolicy.Actor(2L,false,true)));
            when(service.handle(new GetRentalLocationQuery(1L,2L,"Lima","es"))).thenReturn(new GeographicLocation("Lima",-12,-77,"place"));
            var response = controller.getLocations(1L,"Lima",auth);
            assertEquals(200,response.getStatusCode().value()); assertEquals("no-store",response.getHeaders().getCacheControl());
            assertEquals("place",response.getBody().placeId());
        } finally { LocaleContextHolder.resetLocaleContext(); }
    }
    @Test void constructionRoleCannotCallProvider() {
        when(actors.current(auth)).thenReturn(Optional.of(new RentalActorPolicy.Actor(2L,true,false)));
        assertEquals(FORBIDDEN,assertThrows(GeographicLookupException.class,()->controller.getLocations(1L,"Lima",auth)).code()); verifyNoInteractions(service);
    }
    @Test void unavailableIsLocalizedAndNeverContainsProviderDetails() {
        try {
            LocaleContextHolder.setLocale(Locale.forLanguageTag("es-PE"));
            var es = controller.handle(new GeographicLookupException(UNAVAILABLE));
            assertEquals(503,es.getStatusCode().value()); assertEquals("Mapas no disponibles",es.getBody().getTitle());
            assertEquals("RENTAL_LOCATION_UNAVAILABLE",es.getBody().getProperties().get("code"));
            LocaleContextHolder.setLocale(Locale.US);
            var en = controller.handle(new GeographicLookupException(UNAVAILABLE));
            assertEquals("Maps unavailable",en.getBody().getTitle()); assertNotEquals(es.getBody().getDetail(),en.getBody().getDetail());
        } finally { LocaleContextHolder.resetLocaleContext(); }
    }
}

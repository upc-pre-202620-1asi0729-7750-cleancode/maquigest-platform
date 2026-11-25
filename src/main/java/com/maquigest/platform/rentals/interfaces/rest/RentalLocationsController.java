package com.maquigest.platform.rentals.interfaces.rest;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;


import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import com.maquigest.platform.rentals.application.queryservices.RentalLocationQueryService;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalLocationQuery;
import com.maquigest.platform.rentals.interfaces.rest.resources.RentalLocationResource;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.rentals.interfaces.rest.transform.RentalLocationResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;
import java.util.ResourceBundle;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/rentals/{rentalId}/locations", produces=APPLICATION_JSON_VALUE)
@Tag(name="Rental Locations", description="Optional delivery and return location planning")
@SecurityRequirement(name="bearerAuth")
public class RentalLocationsController {
    private final RentalLocationQueryService queries;
    private final RentalActorPolicy policy;
    public RentalLocationsController(RentalLocationQueryService queries, RentalActorPolicy policy) {
        this.queries = queries; this.policy = policy;
    }
    @GetMapping
    @Operation(summary="Geocode a planning address for an owned rental; no lifecycle changes")
    public ResponseEntity<RentalLocationResource> getLocations(@PathVariable Long rentalId,
            @RequestParam(required=false) String address, Authentication auth) {
        var actor = policy.current(auth).filter(RentalActorPolicy.Actor::rentalCompany)
                .orElseThrow(() -> new GeographicLookupException(GeographicLookupException.Code.FORBIDDEN));
        var location = queries.handle(new GetRentalLocationQuery(rentalId, actor.userId(), address,
                LocaleContextHolder.getLocale().getLanguage()));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(RentalLocationResourceAssembler.from(location));
    }
    @ExceptionHandler(GeographicLookupException.class)
    public ResponseEntity<ProblemDetail> handle(GeographicLookupException failure) {
        var code = "RENTAL_LOCATION_" + failure.code().name();
        var locale = "es".equals(LocaleContextHolder.getLocale().getLanguage()) ? Locale.forLanguageTag("es") : Locale.ENGLISH;
        var bundle = ResourceBundle.getBundle("rental-locations", locale, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        var problem = ProblemDetailFromApplicationErrorAssembler.toProblemDetailFromApplicationError(
                new ApplicationError(code, bundle.getString(code)), bundle.getString(code + ".title"));
        return ResponseEntity.status(problem.getStatus()).cacheControl(CacheControl.noStore()).body(problem);
    }
}

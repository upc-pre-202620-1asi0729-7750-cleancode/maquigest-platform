package com.maquigest.platform.shared.interfaces.rest.transform;

import com.maquigest.platform.inventory.application.commandservices.EquipmentCommandService;
import com.maquigest.platform.inventory.application.queryservices.EquipmentQueryService;
import com.maquigest.platform.inventory.interfaces.rest.EquipmentController;
import com.maquigest.platform.inventory.interfaces.rest.security.InventoryOwnershipPolicy;
import com.maquigest.platform.profiles.application.commandservices.CompanyProfileCommandService;
import com.maquigest.platform.profiles.application.queryservices.CompanyProfileQueryService;
import com.maquigest.platform.profiles.interfaces.rest.ProfilesController;
import com.maquigest.platform.profiles.interfaces.rest.resources.UpdateCompanyProfileResource;
import com.maquigest.platform.profiles.interfaces.rest.security.ProfileOwnershipPolicy;
import com.maquigest.platform.subscriptions.application.commandservices.UserSubscriptionCommandService;
import com.maquigest.platform.subscriptions.application.queryservices.UserSubscriptionQueryService;
import com.maquigest.platform.subscriptions.interfaces.rest.UserSubscriptionsController;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.interfaces.rest.MaintenanceExceptionHandler;
import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.queryservices.RentalLocationQueryService;
import com.maquigest.platform.rentals.interfaces.rest.RentalLocationsController;
import com.maquigest.platform.rentals.interfaces.rest.RentalsExceptionHandler;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure;
import com.maquigest.platform.subscriptions.application.commandservices.SubscriptionCheckoutCommandService;
import com.maquigest.platform.subscriptions.interfaces.rest.SubscriptionCheckoutsController;
import com.maquigest.platform.subscriptions.interfaces.rest.security.SubscriptionOwnershipPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProblemDetailConsistencyTests {
    private static final Locale ES = Locale.forLanguageTag("es-PE");
    private static final String SECRET = "password=internal-only";

    @AfterEach void resetLocale() { LocaleContextHolder.resetLocaleContext(); }

    private static ProblemDetail assertContract(ResponseEntity<?> response, int status, String code) {
        assertEquals(status, response.getStatusCode().value());
        var problem = assertInstanceOf(ProblemDetail.class, response.getBody());
        assertEquals(status, problem.getStatus());
        assertEquals(code, problem.getProperties().get("code"));
        return problem;
    }

    private static void assertLegacyType(ProblemDetail problem) {
        // Before A08 these paths used the Spring factory without calling setType.
        // The raw Java object's default type is framework-version dependent.
        var legacy = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(problem.getStatus()), "legacy");
        assertEquals(legacy.getType(), problem.getType());
    }

    @Test void commonValidationPreservesTypeAndDetails() {
        LocaleContextHolder.setLocale(ES);
        var problem = assertContract(toResponseFromApplicationError(
                ApplicationError.validationError("id", "id must be positive")), 400, "VALIDATION_ERROR");
        assertEquals(URI.create("/api/v1/errors/validation-error"), problem.getType());
        assertEquals("Validation Error", problem.getTitle());
        assertEquals("Validación fallida", problem.getDetail());
        assertEquals("id must be positive", problem.getProperties().get("details"));
    }

    @Test void directUnexpectedResultDoesNotExposeTechnicalDetails() {
        LocaleContextHolder.setLocale(ES);
        var result = Result.<String, ApplicationError>failure(ApplicationError.unexpected(SECRET, SECRET));
        var problem = assertContract(ResponseEntityAssembler.toResponseEntityFromResult(result, value -> value, HttpStatus.OK),
                500, "UNEXPECTED_ERROR");
        assertEquals("Error inesperado", problem.getDetail());
        assertEquals("An unexpected error occurred", problem.getProperties().get("details"));
        assertFalse(problem.toString().contains(SECRET));
    }

    @Test void unknownServerErrorIsSanitizedInBothAssemblyPaths() {
        var error = new ApplicationError("INTERNAL_FAILURE", SECRET, SECRET);
        // ROOT selects the base English bundle even on a Spanish-default JVM.
        // Also exercise explicit English with the existing ResourceBundle fallback policy.
        for (var locale : java.util.List.of(Locale.ROOT, Locale.ENGLISH, ES)) {
            LocaleContextHolder.setLocale(locale);
            String expected = locale.equals(Locale.ROOT) ? "Unexpected error"
                    : locale.equals(ES) ? "Error inesperado"
                    : ResourceBundle.getBundle("messages", locale).getString("error.unexpected.message");
            for (var response : java.util.List.of(toResponseFromApplicationError(error), toResponseFromApplicationError(error, SECRET))) {
                var problem = assertContract(response, 500, error.code());
                assertEquals(expected, problem.getDetail());
                assertEquals("An unexpected error occurred", problem.getProperties().get("details"));
                assertFalse(problem.toString().contains(SECRET));
            }
        }
    }

    @Test void legacyForbiddenContractsKeepDefaultTypeAndNoDetailsExtension() {
        var equipment = new EquipmentController(mock(EquipmentCommandService.class), mock(EquipmentQueryService.class), mock(InventoryOwnershipPolicy.class));
        var profiles = new ProfilesController(mock(CompanyProfileCommandService.class), mock(CompanyProfileQueryService.class), mock(ProfileOwnershipPolicy.class));
        var subscriptions = new UserSubscriptionsController(mock(UserSubscriptionQueryService.class), mock(UserSubscriptionCommandService.class), mock(SubscriptionOwnershipPolicy.class));
        var resource = new UpdateCompanyProfileResource(1L, 2L, "QA", "Company", "qa@example.com", "", "QA Company", null);
        var auth = mock(org.springframework.security.core.Authentication.class);
        var equipmentError = assertContract(equipment.delete(1L, auth), 403, "EQUIPMENT_FORBIDDEN");
        var profileError = assertContract(profiles.update(1L, resource, auth), 403, "PROFILE_FORBIDDEN");
        var subscriptionError = assertContract(subscriptions.cancel(1L, auth), 403, "SUBSCRIPTION_FORBIDDEN");
        assertEquals("Only the authenticated rental company that owns the equipment may modify it", equipmentError.getDetail());
        assertEquals("Only the authenticated company may create or update its own profile", profileError.getDetail());
        assertEquals("Only the authenticated rental company may manage its own subscription", subscriptionError.getDetail());
        for (var problem : java.util.List.of(equipmentError, profileError, subscriptionError)) {
            assertLegacyType(problem);
            assertEquals("Forbidden", problem.getTitle());
            assertFalse(problem.getProperties().containsKey("details"));
        }
    }

    @Test void exceptionalProviderCodesHaveExplicitSharedStatusMappings() {
        var statuses = Map.of("CHECKOUT_UNAVAILABLE", 503, "CHECKOUT_PROVIDER", 503,
                "RENTAL_LOCATION_UNAVAILABLE", 503, "RENTAL_LOCATION_NO_RESULTS", 404,
                "BUSINESS_RULE_VIOLATION", 422, "IAM_UNAUTHORIZED", 401);
        statuses.forEach((code, status) -> assertEquals(status.intValue(), toStatusFromErrorCode(code).value()));
    }

    @Test void rentalsAndMaintenancePreserveTheirDifferentLocalizedContracts() {
        for (var locale : java.util.List.of(Locale.ROOT, Locale.ENGLISH, ES)) {
            LocaleContextHolder.setLocale(locale);
            var rental = assertContract(new RentalsExceptionHandler().handle(RentalsException.conflict("private description")),
                    409, "RENTALS_CONFLICT");
            assertLegacyType(rental);
            assertEquals("Conflict", rental.getTitle());
            assertEquals(ResourceBundle.getBundle("rentals", locale).getString("RENTALS_CONFLICT"), rental.getDetail());
            var maintenance = assertContract(new MaintenanceExceptionHandler().handle(MaintenanceException.conflict("Technical inspection must be confirmed")),
                    409, "MAINTENANCE_CONFLICT");
            assertLegacyType(maintenance);
            assertEquals(ResourceBundle.getBundle("maintenance", locale).getString("maintenance.error.title"), maintenance.getTitle());
            assertEquals("Technical inspection must be confirmed", maintenance.getDetail());
        }
    }

    @Test void geographicFailuresPreserveLocalizedTitleDetailAndNoStore() {
        var controller = new RentalLocationsController(mock(RentalLocationQueryService.class), mock(RentalActorPolicy.class));
        for (var locale : java.util.List.of(Locale.ROOT, Locale.ENGLISH, ES)) {
            LocaleContextHolder.setLocale(locale);
            var bundle = ResourceBundle.getBundle("rental-locations", locale, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
            for (var value : GeographicLookupException.Code.values()) {
                var code = "RENTAL_LOCATION_" + value.name();
                int status = switch(value) { case INVALID -> 400; case FORBIDDEN -> 403; case NOT_FOUND, NO_RESULTS -> 404; case UNAVAILABLE -> 503; };
                var response = controller.handle(new GeographicLookupException(value));
                var problem = assertContract(response, status, code);
                assertLegacyType(problem);
                assertEquals(bundle.getString(code + ".title"), problem.getTitle());
                assertEquals(bundle.getString(code), problem.getDetail());
                assertEquals("no-store", response.getHeaders().getCacheControl());
            }
        }
    }

    @Test void checkoutErrorsPreserveAllCodesTitlesAndLocalizedDetails() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        messages.setDefaultEncoding("UTF-8");
        var controller = new SubscriptionCheckoutsController(mock(SubscriptionCheckoutCommandService.class),
                mock(SubscriptionOwnershipPolicy.class), messages);
        for (var locale : java.util.List.of(Locale.ROOT, Locale.ENGLISH, ES)) {
            LocaleContextHolder.setLocale(locale);
            for (var value : CheckoutFailure.Code.values()) {
                var code = "CHECKOUT_" + value.name();
                int status = switch(value) { case UNAVAILABLE, PROVIDER -> 503; case FORBIDDEN -> 403; case NOT_FOUND -> 404; case CONFLICT -> 409; case INVALID -> 400; };
                var problem = assertContract(controller.error(new CheckoutFailure(value)), status, code);
                assertLegacyType(problem);
                assertEquals(HttpStatus.valueOf(status).getReasonPhrase(), problem.getTitle());
                assertEquals(ResourceBundle.getBundle("messages", locale).getString("subscriptions.checkout." + value.name().toLowerCase(Locale.ROOT)), problem.getDetail());
                assertFalse(problem.getProperties().containsKey("details"));
            }
        }
    }
}

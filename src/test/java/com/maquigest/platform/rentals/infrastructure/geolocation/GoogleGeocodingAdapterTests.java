package com.maquigest.platform.rentals.infrastructure.geolocation;

import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import static com.maquigest.platform.rentals.application.exceptions.GeographicLookupException.Code.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GoogleGeocodingAdapterTests {
    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final GoogleGeocodingAdapter adapter = new GoogleGeocodingAdapter(builder.build(), true, "fake-key");
    @Test void mapsValidResponseAndEncodesAddress() {
        server.expect(request -> assertTrue(request.getURI().getRawPath().endsWith("/Av.%20Lima%20%26%20Calle%201%2C%20Peru")))
            .andExpect(header("X-Goog-Api-Key", "fake-key")).andExpect(queryParam("languageCode", "es"))
            .andRespond(withSuccess("""
                {"results":[{"formattedAddress":"Lima, Peru","placeId":"test-place","location":{"latitude":-12.05,"longitude":-77.04}}]}
                """, MediaType.APPLICATION_JSON));
        var result = adapter.locate("Av. Lima & Calle 1, Peru", "es");
        assertEquals(-12.05, result.latitude()); assertEquals(-77.04, result.longitude());
        assertEquals("test-place", result.placeId()); assertEquals("Lima, Peru", result.formattedAddress()); server.verify();
    }
    @Test void zeroResultsIsNotProviderFailure() {
        server.expect(header("X-Goog-Api-Key", "fake-key")).andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));
        assertEquals(NO_RESULTS, assertThrows(GeographicLookupException.class, () -> adapter.locate("missing", "en")).code()); server.verify();
    }
    @Test void rejectedProviderDoesNotLeakBodyOrKey() {
        server.expect(header("X-Goog-Api-Key", "fake-key")).andRespond(withStatus(HttpStatus.FORBIDDEN).body("fake-key private"));
        var failure = assertThrows(GeographicLookupException.class, () -> adapter.locate("Lima", "en"));
        assertEquals(UNAVAILABLE, failure.code()); assertNull(failure.getCause()); assertFalse(failure.getMessage().contains("fake-key")); server.verify();
    }
    @Test void httpErrorIsSanitized() {
        server.expect(header("X-Goog-Api-Key", "fake-key")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).body("fake-key"));
        var failure = assertThrows(GeographicLookupException.class, () -> adapter.locate("Lima", "en"));
        assertEquals(UNAVAILABLE, failure.code()); assertNull(failure.getCause()); server.verify();
    }
    @Test void disabledOrMissingKeyDoesNotMakeRequest() {
        assertEquals(UNAVAILABLE, assertThrows(GeographicLookupException.class, () -> new GoogleGeocodingAdapter(builder.build(), false, "fake-key").locate("Lima", "en")).code());
        assertEquals(UNAVAILABLE, assertThrows(GeographicLookupException.class, () -> new GoogleGeocodingAdapter(builder.build(), true, "").locate("Lima", "en")).code()); server.verify();
    }
}

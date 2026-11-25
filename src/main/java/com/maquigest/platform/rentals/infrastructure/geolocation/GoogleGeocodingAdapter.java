package com.maquigest.platform.rentals.infrastructure.geolocation;

import com.maquigest.platform.rentals.application.exceptions.GeographicLookupException;
import static com.maquigest.platform.rentals.application.exceptions.GeographicLookupException.Code.*;
import com.maquigest.platform.rentals.application.ports.RentalGeocodingPort;
import com.maquigest.platform.rentals.domain.model.valueobjects.GeographicLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
public class GoogleGeocodingAdapter implements RentalGeocodingPort {
    private final RestClient client;
    private final boolean enabled;
    private final String key;
    @Autowired
    public GoogleGeocodingAdapter(@Value("${maquigest.maps.enabled:false}") boolean enabled,
            @Value("${GOOGLE_MAPS_API_KEY:}") String key) {
        this(createClient(), enabled, key);
    }
    GoogleGeocodingAdapter(RestClient client, boolean enabled, String key) {
        this.client = client; this.enabled = enabled; this.key = key;
    }
    private static RestClient createClient() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(8));
        return RestClient.builder().requestFactory(factory).build();
    }
    public GeographicLocation locate(String address, String language) {
        if (!enabled || key == null || key.isBlank()) throw new GeographicLookupException(UNAVAILABLE);
        try {
            var uri = UriComponentsBuilder.fromUriString("https://geocode.googleapis.com/v4/geocode/address")
                    .pathSegment("{address}").queryParam("languageCode", "{language}")
                    .queryParam("regionCode", "PE").encode()
                    .buildAndExpand(Map.of("address", address, "language", language)).toUri();
            var body = client.get().uri(uri).header("X-Goog-Api-Key", key).retrieve().body(Map.class);
            return parse(body);
        } catch (GeographicLookupException e) { throw e; }
        catch (RuntimeException e) { throw new GeographicLookupException(UNAVAILABLE); }
    }
    static GeographicLocation parse(Map<?, ?> body) {
        if (body == null) throw new GeographicLookupException(UNAVAILABLE);
        if (body.isEmpty()) throw new GeographicLookupException(NO_RESULTS);
        if (!(body.get("results") instanceof java.util.List<?> results)) throw new GeographicLookupException(UNAVAILABLE);
        if (results.isEmpty()) throw new GeographicLookupException(NO_RESULTS);
        if (!(results.getFirst() instanceof Map<?, ?> result)
                || !(result.get("location") instanceof Map<?, ?> location)
                || !(location.get("latitude") instanceof Number lat) || !(location.get("longitude") instanceof Number lng)
                || !(result.get("formattedAddress") instanceof String address)
                || !(result.get("placeId") instanceof String placeId)) throw new GeographicLookupException(UNAVAILABLE);
        try { return new GeographicLocation(address, lat.doubleValue(), lng.doubleValue(), placeId); }
        catch (IllegalArgumentException e) { throw new GeographicLookupException(UNAVAILABLE); }
    }
}

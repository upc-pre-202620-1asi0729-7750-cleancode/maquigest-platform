package com.maquigest.platform.shared.infrastructure.email;

import com.maquigest.platform.shared.application.email.EmailGateway;
import com.maquigest.platform.shared.application.email.EmailMessage;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SendGridEmailAdapter implements EmailGateway {
    private final RestClient client;
    private final boolean enabled;
    private final String apiKey;
    private final String fromEmail;
    private final String fromName;

    @Autowired
    public SendGridEmailAdapter(@Value("${maquigest.email.enabled:false}") boolean enabled,
            @Value("${SENDGRID_API_KEY:}") String apiKey,
            @Value("${SENDGRID_FROM_EMAIL:}") String fromEmail,
            @Value("${SENDGRID_FROM_NAME:MaquiGest}") String fromName) {
        this(createClient(), enabled, apiKey, fromEmail, fromName);
    }

    SendGridEmailAdapter(RestClient client, boolean enabled, String apiKey, String fromEmail, String fromName) {
        if (enabled && (apiKey.isBlank() || fromEmail.isBlank() || fromName.isBlank())) {
            throw new IllegalStateException("SendGrid configuration incomplete");
        }
        this.client = client;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    private static RestClient createClient() {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().baseUrl("https://api.sendgrid.com").requestFactory(factory).build();
    }

    @Override public boolean isEnabled() { return enabled; }

    @Override public void send(EmailMessage message) {
        if (!enabled) throw new IllegalStateException("Email transport disabled");
        var payload = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", message.recipient())))),
                "from", Map.of("email", fromEmail, "name", fromName),
                "subject", message.subject(),
                "content", List.of(Map.of("type", "text/plain", "value", message.body())),
                "tracking_settings", Map.of("click_tracking", Map.of("enable", false, "enable_text", false),
                        "open_tracking", Map.of("enable", false)));
        try {
            var response = client.post().uri("/v3/mail/send")
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().toBodilessEntity();
            if (response.getStatusCode().value() != 202) throw new IllegalStateException("Unexpected email provider status");
        } catch (RestClientException exception) {
            // Do not retain provider payload, request headers, secrets or message body.
            throw new IllegalStateException("SendGrid request failed");
        }
    }
}

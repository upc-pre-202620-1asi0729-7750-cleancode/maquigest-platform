package com.maquigest.platform.shared.infrastructure.email;

import com.maquigest.platform.shared.application.email.EmailMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class SendGridEmailAdapterTests {
    private final EmailMessage message = new EmailMessage("recipient@example.com", "Rental request", "Test body");

    @Test void sendsJsonWithBearerAndAccepts202() {
        var builder = RestClient.builder().baseUrl("https://api.sendgrid.com");
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new SendGridEmailAdapter(builder.build(), true, "test-key", "sender@example.com", "MaquiGest");
        server.expect(requestTo("https://api.sendgrid.com/v3/mail/send"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.personalizations[0].to[0].email").value("recipient@example.com"))
                .andExpect(jsonPath("$.from.email").value("sender@example.com"))
                .andExpect(jsonPath("$.subject").value("Rental request"))
                .andExpect(jsonPath("$.content[0].value").value("Test body"))
                .andExpect(jsonPath("$.tracking_settings.click_tracking.enable").value(false))
                .andRespond(withStatus(HttpStatus.ACCEPTED));
        adapter.send(message);
        server.verify();
    }

    @Test void rejectedResponseDoesNotExposeProviderPayload() {
        var builder = RestClient.builder().baseUrl("https://api.sendgrid.com");
        var server = MockRestServiceServer.bindTo(builder).build();
        var adapter = new SendGridEmailAdapter(builder.build(), true, "test-key", "sender@example.com", "MaquiGest");
        server.expect(requestTo("https://api.sendgrid.com/v3/mail/send"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("private-provider-response"));
        var failure = assertThrows(IllegalStateException.class, () -> adapter.send(message));
        assertFalse(failure.getMessage().contains("private-provider-response"));
        assertNull(failure.getCause());
        server.verify();
    }

    @Test void disabledTransportRejectsSendingAndEnabledRequiresCredentials() {
        var adapter = new SendGridEmailAdapter(RestClient.create(), false, "", "", "MaquiGest");
        assertFalse(adapter.isEnabled());
        assertThrows(IllegalStateException.class, () -> adapter.send(message));
        assertThrows(IllegalStateException.class, () -> new SendGridEmailAdapter(RestClient.create(), true, "", "", "MaquiGest"));
    }
}

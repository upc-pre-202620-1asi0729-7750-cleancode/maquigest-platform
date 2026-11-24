package com.maquigest.platform.rentals.interfaces.rest;

import com.maquigest.platform.rentals.application.commandservices.NotificationCommandService;
import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "maquigest.notificationPersistenceTests", matches = "true")
class NotificationRestMySqlTests {
    private static final String PATH = "/api/v1/rental-notifications";
    private static final String PASSWORD = "MaquiGestHttpTest#2026";
    @Autowired private MockMvc mockMvc;
    @Autowired private NotificationCommandService commands;
    @Autowired private NotificationRepository repository;
    @Autowired private DataSource dataSource;
    private Account rental;
    private Account construction;
    private record Account(long id, String email, String token) { }

    @DynamicPropertySource
    static void requireIsolatedSchema(DynamicPropertyRegistry registry) {
        var expected = System.getProperty("maquigest.notificationExpectedDb", "");
        var url = System.getenv("SPRING_DATASOURCE_URL");
        if (!expected.matches("maquigest_ddd_notification_[a-z0-9]{8}") || url == null ||
                !url.startsWith("jdbc:mysql://localhost:3306/" + expected + "?")) {
            throw new IllegalStateException("Notification REST tests require an isolated MySQL schema");
        }
        registry.add("spring.datasource.url", () -> url);
    }

    @BeforeEach
    void registerIsolatedTestAccounts() throws Exception {
        try (var connection = dataSource.getConnection()) {
            assertEquals(System.getProperty("maquigest.notificationExpectedDb"), connection.getCatalog());
        }
        String suffix = UUID.randomUUID().toString().replace("-", "");
        rental = signUpAndSignIn("notification-rental-" + suffix + "@example.test", "rental_company");
        construction = signUpAndSignIn("notification-construction-" + suffix + "@example.test", "construction_company");
    }

    private Long create(Account account, long reference, NotificationType type, Instant occurredAt) {
        return commands.handle(new CreateNotificationCommand(new NotificationRecipientId(account.id()),
                new NotificationReferenceId(reference), type, occurredAt)).toOptional().orElseThrow();
    }

    @Test void listsOnlyOwnedNotificationsInDescendingOrderAndLocalizesDetail() throws Exception {
        var now = Instant.now().minusSeconds(30);
        var older = create(rental, 111L, NotificationType.RENTAL_REQUEST_CREATED, now);
        var newer = create(rental, 112L, NotificationType.RENTAL_REQUEST_CREATED, now.plusSeconds(5));
        create(construction, 113L, NotificationType.RENTAL_REQUEST_APPROVED, now.plusSeconds(10));
        mockMvc.perform(get(PATH).header("Authorization", bearer(rental)).header("Accept-Language", "es-PE")
                        .locale(Locale.forLanguageTag("es-PE")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(newer.intValue()))
                .andExpect(jsonPath("$[1].id").value(older.intValue()))
                .andExpect(jsonPath("$[0].title").value("Solicitud de alquiler recibida"));
        mockMvc.perform(get(PATH + "/" + newer).header("Authorization", bearer(rental)).header("Accept-Language", "en")
                        .locale(Locale.ENGLISH))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Rental request received"));
    }

    @Test void rejectsCrossRecipientReadsAndWrites() throws Exception {
        var id = create(rental, 121L, NotificationType.RENTAL_REQUEST_CREATED, Instant.now().minusSeconds(30));
        mockMvc.perform(get(PATH + "/" + id).header("Authorization", bearer(construction)))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch(PATH + "/" + id + "/read").header("Authorization", bearer(construction)))
                .andExpect(status().isNotFound());
        assertFalse(repository.findById(id).orElseThrow().isRead());
    }

    @Test void markAsReadIsIdempotentAndPreservesEmailStatus() throws Exception {
        var id = create(rental, 131L, NotificationType.RENTAL_REQUEST_CREATED, Instant.now().minusSeconds(30));
        mockMvc.perform(patch(PATH + "/" + id + "/read").header("Authorization", bearer(rental)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.emailStatus").value("PENDING"));
        var firstRead = repository.findById(id).orElseThrow().getReadAt();
        assertNotNull(firstRead);
        mockMvc.perform(patch(PATH + "/" + id + "/read").header("Authorization", bearer(rental)))
                .andExpect(status().isOk());
        var saved = repository.findById(id).orElseThrow();
        assertEquals(firstRead, saved.getReadAt());
        assertEquals(NotificationEmailStatus.PENDING, saved.getEmailStatus());
    }

    @Test void protectsEndpointsAndPublishesSwaggerContracts() throws Exception {
        mockMvc.perform(get(PATH)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(PATH + "/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch(PATH + "/1/read")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(PATH + "/0").header("Authorization", bearer(rental)))
                .andExpect(status().isBadRequest());
        var openApi = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(openApi.contains("\"" + PATH + "\""));
        assertTrue(openApi.contains("\"" + PATH + "/{id}/read\""));
    }

    private Account signUpAndSignIn(String email, String role) throws Exception {
        mockMvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Http","lastName":"Tester","email":"%s",
                                "password":"%s","companyName":"ACL HTTP Tests","role":"%s"}
                                """.formatted(email, PASSWORD, role)))
                .andExpect(status().isCreated());
        String json = mockMvc.perform(post("/api/v1/authentication/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new Account(jsonNumber(json, "id"), email, jsonString(json, "token"));
    }

    private static long jsonNumber(String json, String property) {
        var pattern = Pattern.compile("\\\"" + Pattern.quote(property) + "\\\"\\s*:\\s*(\\d+)");
        var matcher = pattern.matcher(json);
        if (!matcher.find()) throw new AssertionError("Missing numeric " + property + " in authentication response");
        return Long.parseLong(matcher.group(1));
    }

    private static String jsonString(String json, String property) {
        var pattern = Pattern.compile("\\\"" + Pattern.quote(property) + "\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
        var matcher = pattern.matcher(json);
        if (!matcher.find()) throw new AssertionError("Missing " + property + " in authentication response");
        return matcher.group(1);
    }

    private static String bearer(Account account) {
        return "Bearer " + account.token();
    }
}

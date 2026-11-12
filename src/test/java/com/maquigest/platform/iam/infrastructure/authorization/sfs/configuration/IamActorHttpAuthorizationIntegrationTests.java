package com.maquigest.platform.iam.infrastructure.authorization.sfs.configuration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP regression for Phase 1C. Tests real JWT authentication, IAM actor snapshots
 * and authorization policies against an isolated MySQL database. These tests
 * intentionally exercise the real REST routes; no authentication-policy mocks.
 *
 * Run only with the dedicated database, using the packaged PowerShell runner.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IamActorHttpAuthorizationIntegrationTests {
    private static final String PASSWORD = "MaquiGestHttpTest#2026";

    @Autowired
    private MockMvc mockMvc;

    private Account rental;
    private Account construction;

    private record Account(long id, String email, String token) { }

    @BeforeAll
    void registerIsolatedTestAccounts() throws Exception {
        // UUID suffixes prevent collisions with prior test runs on the same temporary DB.
        String suffix = UUID.randomUUID().toString().replace("-", "");
        rental = signUpAndSignIn("rental-" + suffix + "@example.test", "rental_company");
        construction = signUpAndSignIn("construction-" + suffix + "@example.test", "construction_company");
    }

    @Test
    void protectedContextEndpointsRejectAnonymousRequestsWith401() throws Exception {
        for (String endpoint : new String[] {
                "/api/v1/authentication/me", "/api/v1/equipment", "/api/v1/profiles",
                "/api/v1/rental-requests", "/api/v1/user-subscriptions", "/api/v1/incidents"
        }) {
            mockMvc.perform(get(endpoint)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void protectedContextEndpointRejectsInvalidBearerTokenWith401() throws Exception {
        mockMvc.perform(get("/api/v1/equipment")
                        .header("Authorization", "Bearer definitely-not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void genuineJwtGrantsAccessToCurrentPrincipal() throws Exception {
        mockMvc.perform(get("/api/v1/authentication/me").header("Authorization", bearer(rental)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/authentication/me").header("Authorization", bearer(construction)))
                .andExpect(status().isOk());
    }

    @Test
    void constructionCompanyCannotDeleteRentalEquipmentWith403() throws Exception {
        mockMvc.perform(delete("/api/v1/equipment/999999999")
                        .header("Authorization", bearer(construction)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rentalCompanyCannotRegisterEquipmentUnderAnotherCompanyWith403() throws Exception {
        mockMvc.perform(post("/api/v1/equipment")
                        .header("Authorization", bearer(rental))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"code":"HTTP-ACL-TEST","name":"HTTP ACL equipment",
                                "description":"Authorization regression test","categoryId":1,
                                "location":"Lima","dailyRate":10.00,"weeklyRate":60.00}
                                """.formatted(construction.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void companyCannotCreateProfileForAnotherCompanyWith403() throws Exception {
        mockMvc.perform(post("/api/v1/profiles")
                        .header("Authorization", bearer(rental))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"firstName":"Other","lastName":"Company",
                                "contactEmail":"other@example.test","phoneNumber":"999999999",
                                "companyName":"Other Company","address":{"street":"Test street",
                                "district":"La Molina","city":"Lima","country":"Peru",
                                "latitude":-12.1,"longitude":-76.9}}
                                """.formatted(construction.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void rentalCompanyCannotSubscribeAnotherCompanyWith403() throws Exception {
        mockMvc.perform(post("/api/v1/user-subscriptions")
                        .header("Authorization", bearer(rental))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"planId":1,"startDate":"2026-11-06T00:00:00Z",
                                "endDate":"2027-11-06T00:00:00Z","status":"ACTIVE","autoRenew":true}
                                """.formatted(construction.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void constructionCompanyCannotReportOwnerIncidentWith403() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .header("Authorization", bearer(construction))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":1,"description":"Authorization regression test",
                                "blocksRental":true}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void constructionCompanyCannotReadUnfilteredCompanyIncidentsWith403() throws Exception {
        mockMvc.perform(get("/api/v1/incidents")
                        .header("Authorization", bearer(construction)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rentalCompanyCannotSubmitConstructionRentalRequestWith403() throws Exception {
        mockMvc.perform(post("/api/v1/rental-requests")
                        .header("Authorization", bearer(rental))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipmentId":1,"constructionUserId":%d,"rentalCompanyUserId":%d,
                                "startDate":"2027-01-01T00:00:00Z","endDate":"2027-01-08T00:00:00Z",
                                "status":"PENDING"}
                                """.formatted(rental.id(), rental.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void authorizedRolesCanReadTheirExistingAccessibleRoutes() throws Exception {
        mockMvc.perform(get("/api/v1/equipment").header("Authorization", bearer(rental)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/rental-requests").header("Authorization", bearer(construction)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/incidents").header("Authorization", bearer(rental)))
                .andExpect(status().isOk());
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

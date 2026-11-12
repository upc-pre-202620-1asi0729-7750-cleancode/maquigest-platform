package com.maquigest.platform.shared.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Published route guard for the six contexts; not a substitute for Angular UI E2E tests. */
@SpringBootTest
@AutoConfigureMockMvc
class PublishedRestApiRegressionTests {
    @Autowired private MockMvc mockMvc;

    @Test void swaggerContainsEstablishedRoutesForAllSixContexts() throws Exception {
        String openApi = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertTrue(openApi.contains("\"paths\""), "Swagger document does not contain paths");
        for (String path : new String[] {
            "/api/v1/authentication/sign-up", "/api/v1/authentication/sign-in",
            "/api/v1/authentication/me", "/api/v1/equipment-categories",
            "/api/v1/equipment", "/api/v1/equipment/{id}",
            "/api/v1/equipment/{id}/availability",
            "/api/v1/profiles", "/api/v1/rental-requests",
            "/api/v1/rental-requests/{id}/period", "/api/v1/rentals",
            "/api/v1/deliveries", "/api/v1/equipment-returns",
            "/api/v1/incidents", "/api/v1/incidents/equipment/{equipmentId}/reactivate",
            "/api/v1/maintenances", "/api/v1/maintenances/{id}/activities",
            "/api/v1/subscription-plans", "/api/v1/user-subscriptions",
            "/api/v1/user-subscriptions/{id}/cancel"
        }) {
            assertTrue(openApi.contains("\"" + path + "\""), () -> "Missing OpenAPI route: " + path);
        }
    }

    @Test void protectedRoutesRejectAnonymousRequestsAcrossContexts() throws Exception {
        for (String path : new String[] {
            "/api/v1/authentication/me", "/api/v1/equipment", "/api/v1/profiles",
            "/api/v1/rental-requests", "/api/v1/rentals", "/api/v1/incidents",
            "/api/v1/maintenances", "/api/v1/user-subscriptions",
            "/api/v1/subscription-plans"
        }) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test void publicInventoryCategoriesRemainPublic() throws Exception {
        mockMvc.perform(get("/api/v1/equipment-categories")).andExpect(status().isOk());
    }
}

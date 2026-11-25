package com.maquigest.platform.shared.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @Autowired @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    // A12 published HTTP inventory, with A13's controller names. No route or verb changes.
    private static final Set<String> EXPECTED_MAPPINGS = Set.of(
        "AuthenticatedPrincipalController GET /api/v1/authentication/me",
        "AuthenticationController POST /api/v1/authentication/sign-in",
        "AuthenticationController POST /api/v1/authentication/sign-up",
        "DeliveriesController GET /api/v1/deliveries",
        "DeliveriesController POST /api/v1/deliveries",
        "EmailVerificationController POST /api/v1/authentication/resend-email-verification",
        "EmailVerificationController POST /api/v1/authentication/verify-email",
        "EquipmentCategoriesController GET /api/v1/equipment-categories",
        "EquipmentController DELETE /api/v1/equipment/{id}",
        "EquipmentController GET /api/v1/equipment",
        "EquipmentController GET /api/v1/equipment/{id}",
        "EquipmentController GET /api/v1/equipment/{id}/availability",
        "EquipmentController PATCH /api/v1/equipment/{id}",
        "EquipmentController PATCH /api/v1/equipment/{id}/operational-status",
        "EquipmentController POST /api/v1/equipment",
        "EquipmentController POST /api/v1/equipment/{id}/availability-blocks",
        "EquipmentReturnsController GET /api/v1/equipment-returns",
        "EquipmentReturnsController POST /api/v1/equipment-returns",
        "IamNotificationsController GET /api/v1/iam-notifications",
        "IamNotificationsController GET /api/v1/iam-notifications/{id}",
        "IamNotificationsController PATCH /api/v1/iam-notifications/{id}/read",
        "IncidentsController GET /api/v1/incidents",
        "IncidentsController GET /api/v1/incidents/{id}",
        "IncidentsController PATCH /api/v1/incidents/{id}",
        "IncidentsController POST /api/v1/incidents",
        "IncidentsController POST /api/v1/incidents/equipment/{equipmentId}/reactivate",
        "InventoryNotificationsController GET /api/v1/inventory-notifications",
        "InventoryNotificationsController GET /api/v1/inventory-notifications/{id}",
        "InventoryNotificationsController PATCH /api/v1/inventory-notifications/{id}/read",
        "MaintenanceNotificationsController GET /api/v1/maintenance-notifications",
        "MaintenanceNotificationsController GET /api/v1/maintenance-notifications/{id}",
        "MaintenanceNotificationsController PATCH /api/v1/maintenance-notifications/{id}/read",
        "MaintenancesController GET /api/v1/maintenances",
        "MaintenancesController GET /api/v1/maintenances/equipment/{equipmentId}/history",
        "MaintenancesController GET /api/v1/maintenances/{id}",
        "MaintenancesController GET /api/v1/maintenances/{id}/activities",
        "MaintenancesController PATCH /api/v1/maintenances/{id}",
        "MaintenancesController POST /api/v1/maintenances",
        "PasswordRecoveryController POST /api/v1/authentication/password-recovery",
        "PasswordRecoveryController POST /api/v1/authentication/reset-password",
        "ProfileNotificationsController GET /api/v1/profile-notifications",
        "ProfileNotificationsController GET /api/v1/profile-notifications/{id}",
        "ProfileNotificationsController PATCH /api/v1/profile-notifications/{id}/read",
        "ProfilesController GET /api/v1/profiles",
        "ProfilesController GET /api/v1/profiles/{id}",
        "ProfilesController POST /api/v1/profiles",
        "ProfilesController PUT /api/v1/profiles/{id}",
        "RentalLocationsController GET /api/v1/rentals/{rentalId}/locations",
        "RentalNotificationsController GET /api/v1/rental-notifications",
        "RentalNotificationsController GET /api/v1/rental-notifications/{id}",
        "RentalNotificationsController PATCH /api/v1/rental-notifications/{id}/read",
        "RentalRequestsController GET /api/v1/rental-requests",
        "RentalRequestsController GET /api/v1/rental-requests/{id}",
        "RentalRequestsController PATCH /api/v1/rental-requests/{id}/period",
        "RentalRequestsController POST /api/v1/rental-requests",
        "RentalRequestsController PUT /api/v1/rental-requests/{id}",
        "RentalsController GET /api/v1/rentals",
        "RentalsController GET /api/v1/rentals/{id}",
        "RentalsController POST /api/v1/rentals",
        "RentalsController PUT /api/v1/rentals/{id}",
        "SubscriptionCheckoutsController POST /api/v1/subscription-checkouts",
        "SubscriptionCheckoutsController POST /api/v1/subscription-checkouts/webhook",
        "SubscriptionCheckoutsController POST /api/v1/subscription-checkouts/{reference}/confirm",
        "SubscriptionPlansController GET /api/v1/subscription-plans",
        "SubscriptionsNotificationsController GET /api/v1/subscriptions-notifications",
        "SubscriptionsNotificationsController GET /api/v1/subscriptions-notifications/{id}",
        "SubscriptionsNotificationsController PATCH /api/v1/subscriptions-notifications/{id}/read",
        "UserSubscriptionsController GET /api/v1/user-subscriptions",
        "UserSubscriptionsController GET /api/v1/user-subscriptions/active-provider-user-ids",
        "UserSubscriptionsController GET /api/v1/user-subscriptions/{id}",
        "UserSubscriptionsController PATCH /api/v1/user-subscriptions/{id}/cancel",
        "UserSubscriptionsController POST /api/v1/user-subscriptions",
        "UserSubscriptionsController PUT /api/v1/user-subscriptions/{id}"
    );

    @Test void collectionControllerNamesAndAllPublishedHttpMappingsRemainStable() {
        Set<String> actual = new HashSet<>();
        handlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            var controller = handler.getBeanType();
            if (!controller.getPackageName().startsWith("com.maquigest.platform.")
                    || !controller.getPackageName().endsWith(".interfaces.rest")) return;
            for (var path : mapping.getPatternValues()) {
                if (!path.startsWith("/api/v1/")) continue;
                assertTrue(!mapping.getMethodsCondition().getMethods().isEmpty(), path);
                for (var verb : mapping.getMethodsCondition().getMethods()) {
                    String key = controller.getSimpleName() + " " + verb.name() + " " + path;
                    assertTrue(actual.add(key), "Duplicate mapping: " + key);
                }
            }
        });
        assertEquals(EXPECTED_MAPPINGS, actual);
    }

    @Test void swaggerContainsEveryPublishedHttpMethodAndPath() throws Exception {
        var response = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        for (String mapping : EXPECTED_MAPPINGS) {
            var parts = mapping.split(" ", 3);
            String jsonPathExpression = "$.paths['" + parts[2] + "']." + parts[1].toLowerCase(Locale.ROOT);
            response.andExpect(jsonPath(jsonPathExpression).exists());
        }
    }


    @Test void swaggerSeparatesRentalRequestInputsFromPublishedResponse() throws Exception {
        var response = mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        var path = "$.paths['/api/v1/rental-requests']";
        var detail = "$.paths['/api/v1/rental-requests/{id}']";
        var input = ".requestBody.content['application/json'].schema['$ref']";
        response.andExpect(jsonPath(path + ".post" + input).value("#/components/schemas/CreateRentalRequestResource"));
        response.andExpect(jsonPath(detail + ".put" + input).value("#/components/schemas/UpdateRentalRequestResource"));
        response.andExpect(jsonPath(detail + ".get.responses['200'].content['application/json'].schema['$ref']")
            .value("#/components/schemas/RentalRequestResource"));
        for (String schema : new String[]{"CreateRentalRequestResource", "UpdateRentalRequestResource", "RentalRequestResource"}) {
            var properties = "$.components.schemas['" + schema + "'].properties";
            response.andExpect(jsonPath(properties).isMap());
            for (String field : new String[]{"id", "equipmentId", "constructionUserId", "rentalCompanyUserId", "startDate", "endDate", "status", "createdAt"}) {
                response.andExpect(jsonPath(properties + "." + field).exists());
            }
        }
    }

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
            "/api/v1/user-subscriptions/active-provider-user-ids",
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
            "/api/v1/subscription-plans", "/api/v1/user-subscriptions/active-provider-user-ids"
        }) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test void publicInventoryCategoriesRemainPublic() throws Exception {
        mockMvc.perform(get("/api/v1/equipment-categories")).andExpect(status().isOk());
    }
}

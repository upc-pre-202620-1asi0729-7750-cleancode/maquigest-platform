package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.inventory.interfaces.rest.EquipmentController;
import com.maquigest.platform.maintenance.interfaces.rest.IncidentsController;
import com.maquigest.platform.maintenance.interfaces.rest.MaintenancesController;
import com.maquigest.platform.profiles.interfaces.rest.CompanyProfilesController;
import com.maquigest.platform.rentals.interfaces.rest.DeliveriesController;
import com.maquigest.platform.rentals.interfaces.rest.EquipmentReturnsController;
import com.maquigest.platform.rentals.interfaces.rest.RentalsController;
import com.maquigest.platform.rentals.interfaces.rest.RentalRequestsController;
import com.maquigest.platform.subscriptions.interfaces.rest.UserSubscriptionsController;
import com.maquigest.platform.inventory.interfaces.rest.security.InventoryOwnershipPolicy;
import com.maquigest.platform.maintenance.interfaces.rest.security.MaintenanceActorPolicy;
import com.maquigest.platform.profiles.interfaces.rest.security.ProfileOwnershipPolicy;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.subscriptions.interfaces.rest.security.SubscriptionOwnershipPolicy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protect the REST entry-layer direction established by the Phase 2 refactor. */
class RestSecurityDependencyTests {
    private static final List<Class<?>> REST_CONTROLLERS = List.of(
        EquipmentController.class,
        IncidentsController.class,
        MaintenancesController.class,
        CompanyProfilesController.class,
        DeliveriesController.class,
        EquipmentReturnsController.class,
        RentalsController.class,
        RentalRequestsController.class,
        UserSubscriptionsController.class
    );
    private static final List<Class<?>> ACTOR_RESOLVERS = List.of(
        InventoryOwnershipPolicy.class,
        MaintenanceActorPolicy.class,
        ProfileOwnershipPolicy.class,
        RentalActorPolicy.class,
        SubscriptionOwnershipPolicy.class
    );

    @Test
    void restControllersDoNotDependOnInfrastructureIamPolicies() {
        for (Class<?> controller : REST_CONTROLLERS) {
            for (var field : controller.getDeclaredFields()) {
                assertFalse(field.getType().getName().contains(".infrastructure.acl.iam."),
                    () -> controller.getName() + " depends on " + field.getType().getName());
            }
            for (Constructor<?> constructor : controller.getDeclaredConstructors()) {
                for (Class<?> parameter : constructor.getParameterTypes()) {
                    assertFalse(parameter.getName().contains(".infrastructure.acl.iam."),
                        () -> controller.getName() + " depends on " + parameter.getName());
                }
            }
        }
    }

    @Test
    void restActorResolversConsumeOnlyThePublishedIamFacade() {
        for (Class<?> resolver : ACTOR_RESOLVERS) {
            assertTrue(resolver.getPackageName().endsWith(".interfaces.rest.security"), resolver.getName());
            assertTrue(java.util.Arrays.stream(resolver.getDeclaredFields())
                    .anyMatch(field -> field.getType() == IamActorFacade.class),
                () -> resolver.getName() + " must consume the IAM public facade");
        }
    }
}

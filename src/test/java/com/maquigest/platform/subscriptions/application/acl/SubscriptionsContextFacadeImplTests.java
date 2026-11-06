package com.maquigest.platform.subscriptions.application.acl;

import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionPlanRepository;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionsContextFacadeImplTests {
    private final UserSubscriptionRepository subscriptions = mock(UserSubscriptionRepository.class);
    private final SubscriptionPlanRepository plans = mock(SubscriptionPlanRepository.class);
    private final SubscriptionsContextFacadeImpl facade = new SubscriptionsContextFacadeImpl(subscriptions, plans);
    private UserSubscription subscription(Long userId, SubscriptionStatus status, Instant start, Instant end) {
        return new UserSubscription(10L, userId, 3L, start, end, status, true);
    }
    private SubscriptionPlan plan(String name, PlanStatus status) {
        return new SubscriptionPlan(3L, name, "Plan", new BigDecimal("79"), "PEN", "MONTHLY", status);
    }
    private void setup(String name) {
        Instant now = Instant.now();
        when(subscriptions.findAll()).thenReturn(List.of(subscription(1L, SubscriptionStatus.ACTIVE,
            now.minusSeconds(3600), now.plusSeconds(86400))));
        when(plans.findById(3L)).thenReturn(Optional.of(plan(name, PlanStatus.ACTIVE)));
    }
    @Test void nullAndInvalidIdsAreDeniedWithoutQueries() {
        assertFalse(facade.hasActiveSubscription(null));
        assertFalse(facade.canManageRentals(0L));
        assertFalse(facade.hasActiveGrowth(-1L));
        verifyNoInteractions(subscriptions,plans);
    }
    @Test void essentialAllowsActiveButNotRentalManagementOrMaintenance() {
        setup("Essential");
        assertTrue(facade.hasActiveSubscription(1L));
        assertFalse(facade.canManageRentals(1L));
        assertFalse(facade.hasActiveGrowth(1L));
    }
    @Test void professionalAllowsManagementButNotMaintenance() {
        setup("pRoFeSsIoNaL");
        assertTrue(facade.hasActiveSubscription(1L));
        assertTrue(facade.canManageRentals(1L));
        assertFalse(facade.hasActiveGrowth(1L));
    }
    @Test void growthAllowsAllThreePermissions() {
        setup("gRoWtH");
        assertTrue(facade.hasActiveSubscription(1L));
        assertTrue(facade.canManageRentals(1L));
        assertTrue(facade.hasActiveGrowth(1L));
    }
    @Test void inactivePlanDeniesAllPermissions() {
        setup("Growth");
        when(plans.findById(3L)).thenReturn(Optional.of(plan("Growth", PlanStatus.INACTIVE)));
        assertFalse(facade.hasActiveSubscription(1L));
        assertFalse(facade.canManageRentals(1L));
        assertFalse(facade.hasActiveGrowth(1L));
    }
    @Test void missingPlanDeniesAllPermissions() {
        setup("Growth");
        when(plans.findById(3L)).thenReturn(Optional.empty());
        assertFalse(facade.hasActiveSubscription(1L));
        assertFalse(facade.canManageRentals(1L));
        assertFalse(facade.hasActiveGrowth(1L));
    }
    @Test void expiredSubscriptionDeniesAllPermissions() {
        Instant now = Instant.now();
        when(subscriptions.findAll()).thenReturn(List.of(subscription(1L, SubscriptionStatus.ACTIVE,
            now.minusSeconds(86400),now.minusSeconds(3600))));
        assertFalse(facade.hasActiveSubscription(1L));
        assertFalse(facade.canManageRentals(1L));
        assertFalse(facade.hasActiveGrowth(1L));
        verifyNoInteractions(plans);
    }
    @Test void cancelledSubscriptionDeniesAllPermissions() {
        Instant now = Instant.now();
        when(subscriptions.findAll()).thenReturn(List.of(subscription(1L, SubscriptionStatus.CANCELLED,
            now.minusSeconds(3600),now.plusSeconds(86400))));
        assertFalse(facade.hasActiveSubscription(1L));
        assertFalse(facade.canManageRentals(1L));
        assertFalse(facade.hasActiveGrowth(1L));
        verifyNoInteractions(plans);
    }
    @Test void ownershipIsRequired() {
        setup("Growth");
        assertFalse(facade.hasActiveSubscription(2L));
        assertFalse(facade.canManageRentals(2L));
        assertFalse(facade.hasActiveGrowth(2L));
    }
    @Test void validSubscriptionAmongOtherUsersSubscriptionsIsRecognized() {
        Instant now=Instant.now();
        when(subscriptions.findAll()).thenReturn(List.of(
            subscription(2L,SubscriptionStatus.ACTIVE,now.minusSeconds(3600),now.plusSeconds(86400)),
            subscription(1L,SubscriptionStatus.ACTIVE,now.minusSeconds(3600),now.plusSeconds(86400))));
        when(plans.findById(3L)).thenReturn(Optional.of(plan("Growth",PlanStatus.ACTIVE)));
        assertTrue(facade.hasActiveGrowth(1L));
    }
}

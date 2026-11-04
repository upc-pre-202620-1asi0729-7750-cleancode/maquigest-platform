package com.maquigest.platform.rentals.infrastructure.acl.subscriptions;
import com.maquigest.platform.subscriptions.domain.model.aggregates.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.*;
import com.maquigest.platform.subscriptions.domain.repositories.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SubscriptionRentalAclAdapterTests {
    @Test void essentialPlanAllowsRequestsButNotRentalManagement(){
        var subscriptions=mock(UserSubscriptionRepository.class);
        var plans=mock(SubscriptionPlanRepository.class);
        when(subscriptions.findAll()).thenReturn(List.of(new UserSubscription(10L,1L,3L,
            Instant.now().minusSeconds(3600),Instant.now().plusSeconds(86400),SubscriptionStatus.ACTIVE,true)));
        when(plans.findById(3L)).thenReturn(Optional.of(new SubscriptionPlan(3L,"Essential","Base",
            new BigDecimal("79"),"PEN","MONTHLY",PlanStatus.ACTIVE)));
        var acl=new SubscriptionRentalAclAdapter(subscriptions,plans);
        assertTrue(acl.hasActiveSubscription(1L));
        assertFalse(acl.canManageRentals(1L));
    }
}

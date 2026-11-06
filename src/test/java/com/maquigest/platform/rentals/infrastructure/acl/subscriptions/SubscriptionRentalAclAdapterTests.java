package com.maquigest.platform.rentals.infrastructure.acl.subscriptions;
import com.maquigest.platform.subscriptions.interfaces.acl.SubscriptionsContextFacade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SubscriptionRentalAclAdapterTests {
    private final SubscriptionsContextFacade subscriptions = mock(SubscriptionsContextFacade.class);
    private final SubscriptionRentalAclAdapter adapter = new SubscriptionRentalAclAdapter(subscriptions);
    @Test void delegatesActiveSubscriptionCheck() {
        when(subscriptions.hasActiveSubscription(1L)).thenReturn(true);
        assertTrue(adapter.hasActiveSubscription(1L));
        verify(subscriptions).hasActiveSubscription(1L);
    }
    @Test void delegatesRentalManagementCheck() {
        when(subscriptions.canManageRentals(1L)).thenReturn(true);
        assertTrue(adapter.canManageRentals(1L));
        verify(subscriptions).canManageRentals(1L);
    }
    @Test void preservesDeniedEntitlement() {
        assertFalse(adapter.hasActiveSubscription(5L));
        assertFalse(adapter.canManageRentals(5L));
    }
}

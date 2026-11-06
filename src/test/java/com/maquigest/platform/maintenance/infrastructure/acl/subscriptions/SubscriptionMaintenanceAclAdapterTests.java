package com.maquigest.platform.maintenance.infrastructure.acl.subscriptions;
import com.maquigest.platform.subscriptions.interfaces.acl.SubscriptionsContextFacade;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SubscriptionMaintenanceAclAdapterTests {
    private final SubscriptionsContextFacade subscriptions = mock(SubscriptionsContextFacade.class);
    private final SubscriptionMaintenanceAclAdapter adapter = new SubscriptionMaintenanceAclAdapter(subscriptions);
    @Test void delegatesGrowthGranted() {
        when(subscriptions.hasActiveGrowth(1L)).thenReturn(true);
        assertTrue(adapter.hasActiveGrowth(1L));
        verify(subscriptions).hasActiveGrowth(1L);
    }
    @Test void preservesGrowthDenied() {
        assertFalse(adapter.hasActiveGrowth(8L));
        verify(subscriptions).hasActiveGrowth(8L);
    }
}

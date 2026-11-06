package com.maquigest.platform.subscriptions.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamSubscriberFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamSubscriberAclAdapterTests {
    private final IamSubscriberFacade facade = mock(IamSubscriberFacade.class);
    private final IamSubscriberAclAdapter adapter = new IamSubscriberAclAdapter(facade);

    @Test
    void delegatesEligibilityAndPreservesResults() {
        when(facade.isActiveRentalCompany(7L)).thenReturn(true);
        assertTrue(adapter.isActiveRentalCompany(7L));
        assertFalse(adapter.isActiveRentalCompany(8L));
        verify(facade).isActiveRentalCompany(7L);
        verify(facade).isActiveRentalCompany(8L);
    }

    @Test
    void delegatesLockToIamWithoutOpeningAnotherTransaction() {
        when(facade.lockActiveRentalCompany(7L)).thenReturn(true);
        assertTrue(adapter.lockActiveRentalCompany(7L));
        assertFalse(adapter.lockActiveRentalCompany(8L));
        verify(facade).lockActiveRentalCompany(7L);
        verify(facade).lockActiveRentalCompany(8L);
    }
}

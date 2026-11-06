package com.maquigest.platform.iam.application.acl;

import com.maquigest.platform.iam.application.ports.IamSubscriberReader;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamSubscriberFacadeImplTests {
    private final IamSubscriberReader reader = mock(IamSubscriberReader.class);
    private final IamSubscriberFacadeImpl facade = new IamSubscriberFacadeImpl(reader);

    @Test
    void delegatesEligibilityWithoutChangingResult() {
        when(reader.isActiveRentalCompany(5L)).thenReturn(true);
        when(reader.isActiveRentalCompany(6L)).thenReturn(false);
        assertTrue(facade.isActiveRentalCompany(5L));
        assertFalse(facade.isActiveRentalCompany(6L));
        verify(reader).isActiveRentalCompany(5L);
        verify(reader).isActiveRentalCompany(6L);
    }

    @Test
    void delegatesPessimisticLockWithoutChangingResult() {
        when(reader.lockActiveRentalCompany(5L)).thenReturn(true);
        assertTrue(facade.lockActiveRentalCompany(5L));
        assertFalse(facade.lockActiveRentalCompany(6L));
        verify(reader).lockActiveRentalCompany(5L);
        verify(reader).lockActiveRentalCompany(6L);
    }

    @Test
    void lockMethodRequiresAnExistingTransaction() throws Exception {
        var annotation = IamSubscriberFacadeImpl.class
                .getMethod("lockActiveRentalCompany", Long.class)
                .getAnnotation(Transactional.class);
        assertNotNull(annotation);
        assertEquals(Propagation.MANDATORY, annotation.propagation());
    }
}

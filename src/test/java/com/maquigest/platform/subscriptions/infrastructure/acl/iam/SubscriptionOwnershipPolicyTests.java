package com.maquigest.platform.subscriptions.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade.ActorSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionOwnershipPolicyTests {
    private static UsernamePasswordAuthenticationToken auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    @Test void rejectsUnauthenticatedAndUnrelatedSubscriber() {
        var iam = mock(IamActorFacade.class);
        var policy = new SubscriptionOwnershipPolicy(iam);
        assertFalse(policy.canManage(null, 1L));
        assertFalse(policy.canManage(auth("carlos.prueba@example.com"), 2L));
        verify(iam).findActiveByEmail("carlos.prueba@example.com");
    }

    @Test void acceptsOnlyActiveOwnerWithRentalRole() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("carlos.prueba@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(1L, true, false, false)));
        var policy = new SubscriptionOwnershipPolicy(iam);
        assertTrue(policy.canManage(auth("carlos.prueba@example.com"), 1L));
        assertFalse(policy.canManage(auth("carlos.prueba@example.com"), 2L));
    }

    @Test void administratorMayManageOwnSubscriptionButNotOthers() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("admin@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(6L, false, false, true)));
        var policy = new SubscriptionOwnershipPolicy(iam);
        assertTrue(policy.canManage(auth("admin@example.com"), 6L));
        assertFalse(policy.canManage(auth("admin@example.com"), 7L));
    }
}

package com.maquigest.platform.rentals.interfaces.rest.security;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade.ActorSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalActorPolicyTests {
    private static UsernamePasswordAuthenticationToken auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    @Test void constructionCompanyMaySubmitRentalRequest() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("construction@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(12L, false, true, false)));
        var actor = new RentalActorPolicy(iam).current(auth("construction@example.com")).orElseThrow();
        assertEquals(12L, actor.userId());
        assertTrue(actor.constructionCompany());
        assertFalse(actor.rentalCompany());
    }

    @Test void administratorWithoutCompanyRoleRemainsExcluded() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("admin@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(5L, false, false, true)));
        assertTrue(new RentalActorPolicy(iam).current(auth("admin@example.com")).isEmpty());
    }

    @Test void missingOrUnauthenticatedActorIsDenied() {
        var iam = mock(IamActorFacade.class);
        var policy = new RentalActorPolicy(iam);
        assertTrue(policy.current(null).isEmpty());
        assertTrue(policy.current(auth("missing@example.com")).isEmpty());
        verify(iam).findActiveByEmail("missing@example.com");
    }
}

package com.maquigest.platform.maintenance.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade.ActorSnapshot;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MaintenanceActorPolicyTests {
    private static UsernamePasswordAuthenticationToken auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    @Test void rentalCompanyIsAllowedToManageMaintenance() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("rental@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(7L, true, false, false)));
        var actor = new MaintenanceActorPolicy(iam).requireOwner(auth("rental@example.com"));
        assertEquals(7L, actor.userId());
        assertTrue(actor.rentalCompany());
    }

    @Test void constructionCompanyCanReadActorButCannotBeOwner() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("construction@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(9L, false, true, false)));
        var policy = new MaintenanceActorPolicy(iam);
        assertTrue(policy.current(auth("construction@example.com")).constructionCompany());
        assertThrows(MaintenanceException.class, () -> policy.requireOwner(auth("construction@example.com")));
    }

    @Test void adminOnlyOrMissingAuthIsForbidden() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("admin@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(10L, false, false, true)));
        var policy = new MaintenanceActorPolicy(iam);
        assertThrows(MaintenanceException.class, () -> policy.current(auth("admin@example.com")));
        assertThrows(MaintenanceException.class, () -> policy.current(null));
    }
}

package com.maquigest.platform.profiles.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade.ActorSnapshot;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileOwnershipPolicyTests {
    private static Authentication authentication(String email) {
        var auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn(email);
        return auth;
    }

    @Test void rejectsOtherUserEvenWhenPayloadClaimsItsId() {
        var iam = mock(IamActorFacade.class);
        var profiles = mock(CompanyProfileRepository.class);
        when(iam.findActiveByEmail("company@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(1L, true, false, false)));
        var policy = new ProfileOwnershipPolicy(iam, profiles);
        assertTrue(policy.canCreate(authentication("company@example.com"), 1L));
        assertFalse(policy.canCreate(authentication("company@example.com"), 2L));
    }

    @Test void rejectsUnauthenticatedRequests() {
        var iam = mock(IamActorFacade.class);
        var policy = new ProfileOwnershipPolicy(iam, mock(CompanyProfileRepository.class));
        assertFalse(policy.canCreate(null, 1L));
        verifyNoInteractions(iam);
    }

    @Test void administratorWithoutCompanyRoleCannotCreateCompanyProfile() {
        var iam = mock(IamActorFacade.class);
        when(iam.findActiveByEmail("admin@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(5L, false, false, true)));
        var policy = new ProfileOwnershipPolicy(iam, mock(CompanyProfileRepository.class));
        assertFalse(policy.canCreate(authentication("admin@example.com"), 5L));
    }
}

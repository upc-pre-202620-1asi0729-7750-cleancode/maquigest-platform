package com.maquigest.platform.profiles.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ProfileOwnershipPolicyTests {
    @Test void rejectsOtherUserEvenWhenPayloadClaimsItsId(){
        var users=mock(UserRepository.class);var profiles=mock(CompanyProfileRepository.class);
        var auth=mock(Authentication.class);when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("company@example.com");
        var role=mock(Role.class);when(role.getName()).thenReturn(Roles.ROLE_RENTAL_COMPANY);
        var account=User.reconstitute(1L,"Ana","Mora","company@example.com","hash","Company",java.util.Set.of(role),null);
        when(users.findByEmail("company@example.com")).thenReturn(Optional.of(account));
        var policy=new ProfileOwnershipPolicy(users,profiles);
        assertTrue(policy.canCreate(auth,1L));assertFalse(policy.canCreate(auth,2L));
    }
    @Test void rejectsUnauthenticatedRequests(){
        var policy=new ProfileOwnershipPolicy(mock(UserRepository.class),mock(CompanyProfileRepository.class));
        assertFalse(policy.canCreate(null,1L));
    }
}

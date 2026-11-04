package com.maquigest.platform.subscriptions.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SubscriptionOwnershipPolicyTests {
    @Test void rejectsUnauthenticatedAndUnrelatedSubscriber() {
        var users=mock(UserRepository.class);
        var policy=new SubscriptionOwnershipPolicy(users);
        assertFalse(policy.canManage(null,1L));
        var auth=new UsernamePasswordAuthenticationToken("carlos.prueba@example.com", "", Set.of());
        assertFalse(policy.canManage(auth,2L));
        verify(users).findByEmail("carlos.prueba@example.com");
    }
    @Test void acceptsOnlyActiveOwnerWithRentalRole() {
        var users=mock(UserRepository.class);
        var policy=new SubscriptionOwnershipPolicy(users);
        var role=new Role(Roles.ROLE_RENTAL_COMPANY);
        var user=User.reconstitute(1L,"Carlos","QA","carlos.prueba@example.com","abc","QA Rental",Set.of(role),UserStatus.ACTIVE);
        when(users.findByEmail("carlos.prueba@example.com")).thenReturn(Optional.of(user));
        var auth=new UsernamePasswordAuthenticationToken("carlos.prueba@example.com","",Set.of());
        assertTrue(policy.canManage(auth,1L));
        assertFalse(policy.canManage(auth,2L));
    }
}

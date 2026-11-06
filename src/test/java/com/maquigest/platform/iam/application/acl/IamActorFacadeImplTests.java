package com.maquigest.platform.iam.application.acl;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamActorFacadeImplTests {
    private static User user(Long id, UserStatus status, Roles... roles) {
        return User.reconstitute(id, "Ana", "Torres", "ana@example.com", "hash", "Empresa",
            java.util.Arrays.stream(roles).map(Role::new).collect(java.util.stream.Collectors.toSet()), status);
    }

    @Test void returnsRolesAndIdOfActiveRentalCompany() {
        var users = mock(UserRepository.class);
        when(users.findByEmail("ana@example.com"))
            .thenReturn(Optional.of(user(7L, UserStatus.ACTIVE, Roles.ROLE_RENTAL_COMPANY)));
        var identity = new IamActorFacadeImpl(users).findActiveByEmail("ana@example.com").orElseThrow();
        assertEquals(7L, identity.userId());
        assertTrue(identity.rentalCompany());
        assertFalse(identity.constructionCompany());
        assertFalse(identity.administrator());
        verify(users).findByEmail("ana@example.com");
    }

    @Test void preservesAllRolesInsteadOfAssumingOneRole() {
        var users = mock(UserRepository.class);
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(
            user(8L, UserStatus.ACTIVE, Roles.ROLE_ADMIN, Roles.ROLE_CONSTRUCTION_COMPANY)));
        var identity = new IamActorFacadeImpl(users).findActiveByEmail("ana@example.com").orElseThrow();
        assertTrue(identity.administrator());
        assertTrue(identity.constructionCompany());
        assertFalse(identity.rentalCompany());
    }

    @Test void excludesInactiveAccounts() {
        var users = mock(UserRepository.class);
        when(users.findByEmail("ana@example.com"))
            .thenReturn(Optional.of(user(7L, UserStatus.INACTIVE, Roles.ROLE_RENTAL_COMPANY)));
        assertTrue(new IamActorFacadeImpl(users).findActiveByEmail("ana@example.com").isEmpty());
    }

    @Test void excludesMissingAccounts() {
        var users = mock(UserRepository.class);
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertTrue(new IamActorFacadeImpl(users).findActiveByEmail("missing@example.com").isEmpty());
    }

    @Test void doesNotQueryRepositoryWhenEmailIsNull() {
        var users = mock(UserRepository.class);
        assertTrue(new IamActorFacadeImpl(users).findActiveByEmail(null).isEmpty());
        verifyNoInteractions(users);
    }
}

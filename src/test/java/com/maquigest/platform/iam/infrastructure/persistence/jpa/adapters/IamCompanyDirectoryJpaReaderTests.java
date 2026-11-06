package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamCompanyDirectoryJpaReaderTests {
    private final UserPersistenceRepository users = mock(UserPersistenceRepository.class);
    private final IamCompanyDirectoryJpaReader reader = new IamCompanyDirectoryJpaReader(users);

    private static UserPersistenceEntity user(Long id, UserStatus status, Roles role) {
        var entity = new UserPersistenceEntity();
        entity.setId(id);
        entity.setFirstName("Ana");
        entity.setLastName("Mora");
        entity.setEmail("ana@example.com");
        entity.setCompanyName("Rental SAC");
        entity.setStatus(status);
        var persistedRole = new RolePersistenceEntity();
        persistedRole.setName(role);
        entity.setRoles(Set.of(persistedRole));
        return entity;
    }

    @Test
    void rejectsInvalidIdsWithoutAccessingPersistence() {
        assertTrue(reader.findActiveCompany(null).isEmpty());
        assertTrue(reader.findActiveCompany(0L).isEmpty());
        assertTrue(reader.findActiveCompany(-1L).isEmpty());
        verifyNoInteractions(users);
    }

    @Test
    void returnsOnlyActiveRentalOrConstructionCompanies() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1L, UserStatus.ACTIVE, Roles.ROLE_RENTAL_COMPANY)));
        when(users.findById(2L)).thenReturn(Optional.of(user(2L, UserStatus.ACTIVE, Roles.ROLE_CONSTRUCTION_COMPANY)));
        when(users.findById(3L)).thenReturn(Optional.of(user(3L, UserStatus.ACTIVE, Roles.ROLE_ADMIN)));
        when(users.findById(4L)).thenReturn(Optional.of(user(4L, UserStatus.INACTIVE, Roles.ROLE_RENTAL_COMPANY)));
        assertEquals("Rental SAC", reader.findActiveCompany(1L).orElseThrow().companyName());
        assertTrue(reader.findActiveCompany(2L).isPresent());
        assertTrue(reader.findActiveCompany(3L).isEmpty());
        assertTrue(reader.findActiveCompany(4L).isEmpty());
    }

    @Test
    void listsOnlyEligibleUsersInOriginalRepositoryOrder() {
        when(users.findAll()).thenReturn(List.of(
                user(1L, UserStatus.ACTIVE, Roles.ROLE_RENTAL_COMPANY),
                user(2L, UserStatus.ACTIVE, Roles.ROLE_ADMIN),
                user(3L, UserStatus.ACTIVE, Roles.ROLE_CONSTRUCTION_COMPANY),
                user(4L, UserStatus.INACTIVE, Roles.ROLE_CONSTRUCTION_COMPANY)));
        assertEquals(List.of(1L, 3L), reader.allActiveCompanies().stream().map(row -> row.id()).toList());
    }
}

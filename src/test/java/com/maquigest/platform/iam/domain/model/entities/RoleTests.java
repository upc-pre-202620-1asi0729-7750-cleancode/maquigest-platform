package com.maquigest.platform.iam.domain.model.entities;

import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleTests {

    @Test
    void shouldUseDefaultUserRoleWhenNoRolesAreProvided() {
        var roles = Role.validateRoleSet(List.of());
        assertEquals(1, roles.size());
        assertEquals(Roles.ROLE_USER, roles.getFirst().getName());
    }

    @Test
    void shouldPreserveExplicitMaquigestBusinessRoles() {
        var role = new Role(Roles.ROLE_RENTAL_COMPANY);
        var roles = Role.validateRoleSet(List.of(role));
        assertEquals(List.of(role), roles);
        assertEquals("ROLE_RENTAL_COMPANY", role.getStringName());
    }

    @Test
    void shouldResolveNamedRole() {
        assertEquals(Roles.ROLE_CONSTRUCTION_COMPANY,
                Role.toRoleFromName("ROLE_CONSTRUCTION_COMPANY").getName());
    }
}

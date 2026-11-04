package com.maquigest.platform.iam.infrastructure.authorization.sfs.model;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTests {
    @Test
    void mapsDomainRolesToSpringAuthorities() {
        var user = User.reconstitute(2L, "Ada", "Lovelace", "ada@example.com", "bcrypt-hash",
                "Machines SAC", Set.of(new Role(Roles.ROLE_RENTAL_COMPANY)), UserStatus.ACTIVE);
        var principal = UserDetailsImpl.build(user);
        assertEquals("ada@example.com", principal.getUsername());
        assertEquals("bcrypt-hash", principal.getPassword());
        assertTrue(principal.isEnabled());
        assertEquals("ROLE_RENTAL_COMPANY", principal.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void mapsInactiveDomainUserToDisabledPrincipal() {
        var user = User.reconstitute(2L, "Ada", "Lovelace", "ada@example.com", "bcrypt-hash",
                "Machines SAC", Set.of(new Role(Roles.ROLE_RENTAL_COMPANY)), UserStatus.INACTIVE);
        assertFalse(UserDetailsImpl.build(user).isEnabled());
    }
}

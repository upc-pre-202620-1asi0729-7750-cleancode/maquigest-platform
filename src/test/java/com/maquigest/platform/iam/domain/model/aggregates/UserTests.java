package com.maquigest.platform.iam.domain.model.aggregates;

import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTests {
    @Test
    void newUserHasActiveStatusAndOneRole() {
        var user = new User("Ada", "Lovelace", "ADA@Example.com", "encoded-bcrypt-hash",
                "Machines SAC", new Role(Roles.ROLE_RENTAL_COMPANY));
        assertEquals("ada@example.com", user.getEmail());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals(1, user.getRoles().size());
        assertNull(user.getId());
    }

    @Test
    void rolesCannotBeChangedOutsideAggregate() {
        var role = new Role(Roles.ROLE_RENTAL_COMPANY);
        var user = new User("Ada", "Lovelace", "ada@example.com", "hash", "Machines SAC", role);
        var snapshot = user.getRoles();
        var added = new Role(Roles.ROLE_USER);
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(added));
        assertEquals(java.util.Set.of(role), user.getRoles());
        user.addRole(added);
        assertEquals(java.util.Set.of(role, added), user.getRoles());
        assertEquals(java.util.Set.of(role), snapshot);
    }

    @Test
    void rejectsMissingCompany() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Ada", "Lovelace", "ada@example.com", "hash", " ",
                        new Role(Roles.ROLE_RENTAL_COMPANY)));
    }

    @Test
    void reconstitutesPersistenceIdentity() {
        var role = new Role(Roles.ROLE_CONSTRUCTION_COMPANY);
        var user = User.reconstitute(7L, "Ada", "Lovelace", "ada@example.com", "hash",
                "Builder SAC", java.util.Set.of(role), UserStatus.INACTIVE);
        assertEquals(7L, user.getId());
        assertEquals(UserStatus.INACTIVE, user.getStatus());
    }
}

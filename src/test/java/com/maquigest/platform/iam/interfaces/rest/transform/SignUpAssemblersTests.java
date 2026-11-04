package com.maquigest.platform.iam.interfaces.rest.transform;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.interfaces.rest.resources.SignUpResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SignUpAssemblersTests {
    @Test
    void mapsAllowedBusinessRole() {
        var request = new SignUpResource("Ada", "Lovelace", "ada@example.com", "SecurePass123",
                "Machines SAC", "rental_company", "active");
        assertEquals(Roles.ROLE_RENTAL_COMPANY,
                SignUpCommandFromResourceAssembler.toCommandFromResource(request).role());
    }

    @Test
    void doesNotAllowPrivilegeEscalation() {
        var request = new SignUpResource("Ada", "Lovelace", "ada@example.com", "SecurePass123",
                "Machines SAC", "ROLE_ADMIN", "active");
        assertThrows(IllegalArgumentException.class,
                () -> SignUpCommandFromResourceAssembler.toCommandFromResource(request));
    }

    @Test
    void registrationResponseDoesNotExposePassword() {
        var user = User.reconstitute(3L, "Ada", "Lovelace", "ada@example.com", "encoded-secret",
                "Machines SAC", java.util.Set.of(new Role(Roles.ROLE_RENTAL_COMPANY)),
                com.maquigest.platform.iam.domain.model.valueobjects.UserStatus.ACTIVE);
        var response = UserResourceFromEntityAssembler.toResourceFromEntity(user);
        assertEquals("rental_company", response.role());
        assertEquals("active", response.status());
        assertEquals("ada@example.com", response.email());
    }
}

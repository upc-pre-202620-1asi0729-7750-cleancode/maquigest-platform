package com.maquigest.platform.iam.interfaces.rest.transform;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.interfaces.rest.resources.SignInResource;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SignInAssemblersTests {
    @Test
    void mapsEmailAndPasswordToCommand() {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(
                new SignInResource("ada@example.com", "CorrectPassword123!"));
        assertEquals("ada@example.com", command.email());
        assertEquals("CorrectPassword123!", command.password());
    }

    @Test
    void responseIncludesOnlyRequiredPublicFields() {
        var user = User.reconstitute(42L, "Ada", "Lovelace", "ada@example.com", "stored-secret-hash",
                "Machines SAC", Set.of(new Role(Roles.ROLE_RENTAL_COMPANY)), UserStatus.ACTIVE);
        var response = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user, "jwt-value");
        assertEquals(42L, response.id());
        assertEquals("ada@example.com", response.email());
        assertEquals("rental_company", response.role());
        assertEquals("active", response.status());
        assertEquals("jwt-value", response.token());
        assertEquals(5, response.getClass().getRecordComponents().length);
    }
}

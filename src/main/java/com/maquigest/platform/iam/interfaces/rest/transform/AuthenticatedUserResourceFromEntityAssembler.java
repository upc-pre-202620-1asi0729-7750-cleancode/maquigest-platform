package com.maquigest.platform.iam.interfaces.rest.transform;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;

import java.util.Locale;

/** Assemble the JWT response expected by MaquiGest Angular WebApp. */
public final class AuthenticatedUserResourceFromEntityAssembler {
    private AuthenticatedUserResourceFromEntityAssembler() {
    }

    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        var role = user.getRoles().stream().map(r -> r.getName())
                .filter(r -> r == Roles.ROLE_RENTAL_COMPANY || r == Roles.ROLE_CONSTRUCTION_COMPANY
                        || r == Roles.ROLE_ADMIN || r == Roles.ROLE_USER)
                .sorted(java.util.Comparator.comparingInt(AuthenticatedUserResourceFromEntityAssembler::priority))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("IAM user has no recognized role"));
        var roleName = switch (role) {
            case ROLE_RENTAL_COMPANY -> "rental_company";
            case ROLE_CONSTRUCTION_COMPANY -> "construction_company";
            case ROLE_ADMIN -> "admin";
            case ROLE_USER -> "user";
        };
        return new AuthenticatedUserResource(user.getId(), user.getEmail(), roleName,
                user.getStatus().name().toLowerCase(Locale.ROOT), token);
    }

    private static int priority(Roles role) {
        return switch (role) {
            case ROLE_ADMIN -> 0;
            case ROLE_RENTAL_COMPANY -> 1;
            case ROLE_CONSTRUCTION_COMPANY -> 2;
            case ROLE_USER -> 3;
        };
    }
}

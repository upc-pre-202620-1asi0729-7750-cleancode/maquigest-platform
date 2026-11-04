package com.maquigest.platform.iam.interfaces.rest.transform;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.interfaces.rest.resources.UserResource;

import java.util.Locale;

/** Converts IAM aggregates to the existing Angular API response schema. */
public final class UserResourceFromEntityAssembler {
    private UserResourceFromEntityAssembler() {
    }

    public static UserResource toResourceFromEntity(User user) {
        var businessRole = user.getRoles().stream()
                .map(role -> role.getName())
                .filter(role -> role == Roles.ROLE_RENTAL_COMPANY || role == Roles.ROLE_CONSTRUCTION_COMPANY)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Business account role missing"));
        var role = businessRole == Roles.ROLE_RENTAL_COMPANY
                ? "rental_company" : "construction_company";
        return new UserResource(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getCompanyName(), role,
                user.getStatus().name().toLowerCase(Locale.ROOT));
    }
}

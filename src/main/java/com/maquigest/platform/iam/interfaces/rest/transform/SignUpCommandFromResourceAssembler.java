package com.maquigest.platform.iam.interfaces.rest.transform;

import com.maquigest.platform.iam.domain.model.commands.SignUpCommand;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.interfaces.rest.resources.SignUpResource;

/** Translate the public role label into an allowed IAM role (never ADMIN). */
public final class SignUpCommandFromResourceAssembler {
    private SignUpCommandFromResourceAssembler() {
    }

    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var role = switch (resource.role()) {
            case "rental_company" -> Roles.ROLE_RENTAL_COMPANY;
            case "construction_company" -> Roles.ROLE_CONSTRUCTION_COMPANY;
            default -> throw new IllegalArgumentException("Unsupported organization type");
        };
        return new SignUpCommand(resource.firstName(), resource.lastName(),
                resource.email(), resource.password(), resource.companyName(), role);
    }
}

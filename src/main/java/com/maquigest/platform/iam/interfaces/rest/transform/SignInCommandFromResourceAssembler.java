package com.maquigest.platform.iam.interfaces.rest.transform;

import com.maquigest.platform.iam.domain.model.commands.SignInCommand;
import com.maquigest.platform.iam.interfaces.rest.resources.SignInResource;

/** Translate the incoming REST resource into the domain sign-in command. */
public final class SignInCommandFromResourceAssembler {
    private SignInCommandFromResourceAssembler() {
    }

    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}

package com.maquigest.platform.iam.interfaces.rest.transform;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.iam.interfaces.rest.resources.*;
public final class EmailVerificationCommandFromResourceAssembler {
    private EmailVerificationCommandFromResourceAssembler() { }
    public static VerifyEmailCommand toCommandFromResource(VerifyEmailResource resource) {
        return new VerifyEmailCommand(resource.token(), resource.password());
    }
    public static ResendEmailVerificationCommand toCommandFromResource(ResendEmailVerificationResource resource) {
        return new ResendEmailVerificationCommand(resource.email());
    }
}

package com.maquigest.platform.iam.interfaces.rest.transform;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.iam.interfaces.rest.resources.*;
public final class PasswordRecoveryCommandFromResourceAssembler {
    private PasswordRecoveryCommandFromResourceAssembler() { }
    public static RequestPasswordRecoveryCommand toCommandFromResource(PasswordRecoveryResource resource) {
        return new RequestPasswordRecoveryCommand(resource.email());
    }
    public static ResetPasswordCommand toCommandFromResource(ResetPasswordResource resource) {
        return new ResetPasswordCommand(resource.token(), resource.password());
    }
}

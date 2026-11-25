package com.maquigest.platform.iam.application.commandservices;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.shared.application.result.*;
public interface EmailVerificationCommandService {
    Result<Boolean, ApplicationError> handle(VerifyEmailCommand command);
    Result<Boolean, ApplicationError> handle(ResendEmailVerificationCommand command);
}

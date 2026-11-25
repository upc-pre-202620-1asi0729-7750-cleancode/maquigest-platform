package com.maquigest.platform.iam.application.commandservices;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.shared.application.result.*;
public interface PasswordRecoveryCommandService {
    Result<Boolean, ApplicationError> handle(RequestPasswordRecoveryCommand command);
    Result<Boolean, ApplicationError> handle(ResetPasswordCommand command);
}

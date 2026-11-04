package com.maquigest.platform.iam.application.commandservices;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.commands.SignUpCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;

/** Application service contract for IAM user commands. */
public interface UserCommandService {
    Result<User, ApplicationError> handle(SignUpCommand command);
}

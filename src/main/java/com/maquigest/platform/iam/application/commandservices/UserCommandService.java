package com.maquigest.platform.iam.application.commandservices;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.commands.SignInCommand;
import com.maquigest.platform.iam.domain.model.commands.SignUpCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;

/** Application service contract for IAM user commands. */
public interface UserCommandService {
    Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command);

    Result<User, ApplicationError> handle(SignUpCommand command);
}

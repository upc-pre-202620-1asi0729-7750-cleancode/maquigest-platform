package com.maquigest.platform.iam.application.commandservices;

import com.maquigest.platform.iam.domain.model.commands.SeedRolesCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;

/**
 * Application service contract for IAM role commands.
 */
public interface RoleCommandService {
    Result<Integer, ApplicationError> handle(SeedRolesCommand command);
}

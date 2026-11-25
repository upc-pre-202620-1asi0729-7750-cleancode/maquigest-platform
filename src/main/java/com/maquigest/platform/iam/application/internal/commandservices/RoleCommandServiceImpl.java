package com.maquigest.platform.iam.application.internal.commandservices;

import com.maquigest.platform.iam.application.commandservices.RoleCommandService;
import com.maquigest.platform.iam.domain.model.commands.SeedRolesCommand;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link RoleCommandService} to handle {@link SeedRolesCommand}.
 */
@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Result<Integer, ApplicationError> handle(SeedRolesCommand command) {
        int created = 0;
        for (var role : Roles.values()) {
            if (!roleRepository.existsByName(role)) {
                roleRepository.save(new Role(role));
                created++;
            }
        }
        return Result.success(created);
    }
}

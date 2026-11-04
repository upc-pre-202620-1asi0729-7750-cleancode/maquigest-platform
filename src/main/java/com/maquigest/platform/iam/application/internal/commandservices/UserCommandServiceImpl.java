package com.maquigest.platform.iam.application.internal.commandservices;

import com.maquigest.platform.iam.application.commandservices.UserCommandService;
import com.maquigest.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.commands.SignUpCommand;
import com.maquigest.platform.iam.domain.repositories.RoleRepository;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** CQRS command handler for user registration. */
@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  HashingService hashingService,
                                  RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Result<User, ApplicationError> handle(SignUpCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()) {
            return Result.failure(ApplicationError.validationError("email", "Email is required"));
        }
        var email = command.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            return Result.failure(ApplicationError.conflict("User", "Email already registered"));
        }
        if (command.password() == null || command.password().length() < 8) {
            return Result.failure(ApplicationError.validationError("password", "Minimum 8 characters"));
        }
        if (command.role() != com.maquigest.platform.iam.domain.model.valueobjects.Roles.ROLE_RENTAL_COMPANY
                && command.role() != com.maquigest.platform.iam.domain.model.valueobjects.Roles.ROLE_CONSTRUCTION_COMPANY) {
            return Result.failure(ApplicationError.validationError("role", "Unsupported business role"));
        }
        var role = roleRepository.findByName(command.role());
        if (role.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Role", command.role().name()));
        }
        var user = new User(command.firstName(), command.lastName(), email,
                hashingService.encode(command.password()), command.companyName(), role.get());
        var savedUser = userRepository.save(user);
        return Result.success(savedUser);
    }
}

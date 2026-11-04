package com.maquigest.platform.iam.application.internal.commandservices;

import com.maquigest.platform.iam.application.commandservices.UserCommandService;
import com.maquigest.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.maquigest.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.commands.SignInCommand;
import com.maquigest.platform.iam.domain.model.commands.SignUpCommand;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.RoleRepository;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** IAM application command handler; preserves Sign-up and adds Sign-in. */
@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository,
                                  HashingService hashingService,
                                  RoleRepository roleRepository,
                                  TokenService tokenService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.roleRepository = roleRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command) {
        if (command == null || command.email() == null || command.email().isBlank()
                || command.password() == null || command.password().isBlank()) {
            return invalidCredentials();
        }
        var email = command.email().trim().toLowerCase(Locale.ROOT);
        var user = userRepository.findByEmail(email);
        // Identical error for missing email, wrong password or disabled account prevents account enumeration.
        if (user.isEmpty() || user.get().getStatus() != UserStatus.ACTIVE) {
            return invalidCredentials();
        }
        if (!hashingService.matches(command.password(), user.get().getPassword())) {
            return invalidCredentials();
        }
        var token = tokenService.generateToken(user.get().getEmail());
        return Result.success(ImmutablePair.of(user.get(), token));
    }

    private static Result<ImmutablePair<User, String>, ApplicationError> invalidCredentials() {
        return Result.failure(ApplicationError.validationError("credentials", "Invalid email or password"));
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
        if (command.role() != Roles.ROLE_RENTAL_COMPANY
                && command.role() != Roles.ROLE_CONSTRUCTION_COMPANY) {
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

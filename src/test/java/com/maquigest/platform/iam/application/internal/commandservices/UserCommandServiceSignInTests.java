package com.maquigest.platform.iam.application.internal.commandservices;

import com.maquigest.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.maquigest.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.commands.SignInCommand;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.RoleRepository;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserCommandServiceSignInTests {
    private final UserRepository users = mock(UserRepository.class);
    private final RoleRepository roles = mock(RoleRepository.class);
    private final HashingService hashing = mock(HashingService.class);
    private final TokenService tokens = mock(TokenService.class);
    private final UserCommandServiceImpl service = new UserCommandServiceImpl(users, hashing, roles, tokens);

    private static User user(UserStatus status) {
        return User.reconstitute(42L, "Ada", "Lovelace", "ada@example.com", "stored-bcrypt-hash",
                "Machines SAC", Set.of(new Role(Roles.ROLE_RENTAL_COMPANY)), status);
    }

    @Test
    void validCredentialsReturnUserAndToken() {
        when(users.findByEmail("ada@example.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(hashing.matches("CorrectPassword123!", "stored-bcrypt-hash")).thenReturn(true);
        when(tokens.generateToken("ada@example.com")).thenReturn("signed-jwt");
        var result = service.handle(new SignInCommand("ADA@EXAMPLE.COM", "CorrectPassword123!"));
        assertTrue(result.isSuccess());
        var authentication = result.toOptional().orElseThrow();
        assertEquals(42L, authentication.getLeft().getId());
        assertEquals("signed-jwt", authentication.getRight());
    }

    @Test
    void nonexistentAccountAndWrongPasswordReturnSameError() {
        when(users.findByEmail("ada@example.com")).thenReturn(Optional.of(user(UserStatus.ACTIVE)));
        when(hashing.matches("WrongPassword123!", "stored-bcrypt-hash")).thenReturn(false);
        var nonexistent = service.handle(new SignInCommand("missing@example.com", "WrongPassword123!"));
        var wrongPassword = service.handle(new SignInCommand("ada@example.com", "WrongPassword123!"));
        assertInstanceOf(Result.Failure.class, nonexistent);
        assertInstanceOf(Result.Failure.class, wrongPassword);
        assertEquals(((Result.Failure<?, ?>) nonexistent).error(), ((Result.Failure<?, ?>) wrongPassword).error());
        verifyNoInteractions(tokens);
    }

    @Test
    void inactiveAccountCannotSignIn() {
        when(users.findByEmail("ada@example.com")).thenReturn(Optional.of(user(UserStatus.INACTIVE)));
        var result = service.handle(new SignInCommand("ada@example.com", "CorrectPassword123!"));
        assertTrue(result.isFailure());
        verifyNoInteractions(tokens);
    }
}

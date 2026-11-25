package com.maquigest.platform.iam.application.internal.commandservices;
import com.maquigest.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.maquigest.platform.iam.domain.model.aggregates.PasswordRecovery;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.maquigest.platform.iam.domain.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
@Service
public class PasswordRecoveryTransactions {
    private final UserRepository users;
    private final PasswordRecoveryRepository recoveries;
    private final HashingService hashing;
    public PasswordRecoveryTransactions(UserRepository users, PasswordRecoveryRepository recoveries, HashingService hashing) {
        this.users = users; this.recoveries = recoveries; this.hashing = hashing;
    }
    public record IssuedGrant(String email, String token) {
        @Override public String toString() { return "IssuedGrant[redacted]"; }
    }
    @Transactional
    public Optional<IssuedGrant> issue(String email) {
        var found = users.findByEmailForUpdate(email);
        if (found.isEmpty() || found.get().getStatus() != UserStatus.ACTIVE) return Optional.empty();
        var user = found.get();
        var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        var previous = recoveries.findByUserIdForUpdate(user.getId());
        if (previous.isPresent() && !previous.get().canReissueAt(now)) return Optional.empty();
        var raw = PasswordRecoveryTokens.generate();
        recoveries.save(PasswordRecovery.issue(previous.map(PasswordRecovery::getId).orElse(null),
                user.getId(), PasswordRecoveryTokens.hash(raw), now));
        return Optional.of(new IssuedGrant(user.getEmail(), raw));
    }
    @Transactional
    public boolean reset(ResetPasswordCommand command) {
        var hash = PasswordRecoveryTokens.hash(command.token());
        var id = recoveries.findUserIdByTokenHash(hash);
        if (id.isEmpty()) return false;
        // Same lock order as issue; scalar lookup does not cache a stale recovery entity.
        var user = users.findByIdForUpdate(id.get());
        if (user.isEmpty() || user.get().getStatus() != UserStatus.ACTIVE) return false;
        var recovery = recoveries.findByUserIdForUpdate(id.get());
        var now = Instant.now().truncatedTo(ChronoUnit.MICROS);
        if (recovery.isEmpty() || !recovery.get().canConsume(hash, now)) return false;
        user.get().changePassword(hashing.encode(command.password()));
        recovery.get().consume(hash, now);
        users.save(user.get());
        recoveries.save(recovery.get());
        return true;
    }
}

package com.maquigest.platform.iam.application.internal.commandservices;
import com.maquigest.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.maquigest.platform.iam.domain.model.aggregates.*;
import com.maquigest.platform.iam.domain.model.commands.VerifyEmailCommand;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Service
public class EmailVerificationTransactions {
    private final UserRepository users;
    private final EmailVerificationRepository verifications;
    private final HashingService hashing;
    private final ApplicationEventPublisher events;
    public EmailVerificationTransactions(UserRepository users, EmailVerificationRepository verifications,
            HashingService hashing, ApplicationEventPublisher events) {
        this.users = users; this.verifications = verifications; this.hashing = hashing; this.events = events;
    }
    public record DeliveryRequested(String email, String token) {
        @Override public String toString() { return "DeliveryRequested[redacted]"; }
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void issueInitial(User user) {
        if (user.getStatus() != UserStatus.PENDING_EMAIL_VERIFICATION) throw new IllegalStateException("Account not pending");
        issue(user, null, now());
    }
    @Transactional
    public void resend(String email) {
        var user = users.findByEmailForUpdate(email);
        if (user.isEmpty() || user.get().getStatus() != UserStatus.PENDING_EMAIL_VERIFICATION) return;
        var grant = verifications.findByUserIdForUpdate(user.get().getId());
        var now = now();
        if (grant.isEmpty() || !grant.get().canReissueAt(now) || grant.get().getConsumedAt() != null) return;
        issue(user.get(), grant.get().getId(), now);
    }
    private void issue(User user, Long id, Instant now) {
        var token = EmailVerificationTokens.generate();
        verifications.save(EmailVerification.issue(id, user.getId(), EmailVerificationTokens.hash(token), now));
        events.publishEvent(new DeliveryRequested(user.getEmail(), token));
    }
    @Transactional
    public boolean verify(VerifyEmailCommand command) {
        var hash = EmailVerificationTokens.hash(command.token());
        var id = verifications.findUserIdByTokenHash(hash);
        if (id.isEmpty()) return false;
        var user = users.findByIdForUpdate(id.get());
        if (user.isEmpty() || user.get().getStatus() != UserStatus.PENDING_EMAIL_VERIFICATION) return false;
        var grant = verifications.findByUserIdForUpdate(id.get());
        var now = now();
        if (grant.isEmpty() || !grant.get().canConsume(hash, now) || !hashing.matches(command.password(), user.get().getPassword())) return false;
        grant.get().consume(hash, now); user.get().verifyEmail();
        users.save(user.get()); verifications.save(grant.get()); return true;
    }
    private static Instant now() { return Instant.now().truncatedTo(ChronoUnit.MICROS); }
}

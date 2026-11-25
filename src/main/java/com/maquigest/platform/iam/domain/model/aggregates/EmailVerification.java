package com.maquigest.platform.iam.domain.model.aggregates;

import com.maquigest.platform.iam.domain.model.valueobjects.VerificationTokenHash;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.time.Duration;
import java.util.Objects;

/** One verification grant per user. A new grant replaces the previous token. */
@Getter
public final class EmailVerification extends AbstractDomainAggregateRoot<EmailVerification> {
    private final Long id;
    private final Long userId;
    private final VerificationTokenHash tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant consumedAt;

    private EmailVerification(Long id, Long userId, VerificationTokenHash tokenHash,
            Instant issuedAt, Instant expiresAt, Instant consumedAt) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("Invalid user identity");
        this.id = id;
        this.userId = userId;
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        if (!expiresAt.isAfter(issuedAt)) throw new IllegalArgumentException("Invalid verification expiry");
        if (consumedAt != null && (consumedAt.isBefore(issuedAt) || !consumedAt.isBefore(expiresAt)))
            throw new IllegalArgumentException("Invalid consumption time");
        this.consumedAt = consumedAt;
    }
    public static EmailVerification issue(Long id, Long userId, VerificationTokenHash hash, Instant now) {
        return new EmailVerification(id, userId, hash, now, now.plus(Duration.ofHours(24)), null);
    }
    public static EmailVerification reconstitute(Long id, Long userId, VerificationTokenHash hash,
            Instant issuedAt, Instant expiresAt, Instant consumedAt) {
        return new EmailVerification(Objects.requireNonNull(id), userId, hash, issuedAt, expiresAt, consumedAt);
    }
    public boolean canReissueAt(Instant now) { return !now.isBefore(issuedAt.plusSeconds(60)); }
    public boolean canConsume(VerificationTokenHash hash, Instant now) {
        return tokenHash.equals(hash) && consumedAt == null && !now.isBefore(issuedAt) && now.isBefore(expiresAt);
    }
    public void consume(VerificationTokenHash hash, Instant now) {
        if (!canConsume(hash, now)) throw new IllegalStateException("Invalid verification grant");
        consumedAt = now;
    }
}

package com.maquigest.platform.iam.domain.model.aggregates;

import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.time.Duration;
import java.util.Objects;

/** One recovery grant per user. A new grant replaces the previous token. */
@Getter
public final class PasswordRecovery extends AbstractDomainAggregateRoot<PasswordRecovery> {
    private final Long id;
    private final Long userId;
    private final RecoveryTokenHash tokenHash;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private Instant consumedAt;

    private PasswordRecovery(Long id, Long userId, RecoveryTokenHash tokenHash,
            Instant issuedAt, Instant expiresAt, Instant consumedAt) {
        if (userId == null || userId <= 0) throw new IllegalArgumentException("Invalid user identity");
        this.id = id;
        this.userId = userId;
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.issuedAt = Objects.requireNonNull(issuedAt);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        if (!expiresAt.isAfter(issuedAt)) throw new IllegalArgumentException("Invalid recovery expiry");
        if (consumedAt != null && (consumedAt.isBefore(issuedAt) || !consumedAt.isBefore(expiresAt)))
            throw new IllegalArgumentException("Invalid consumption time");
        this.consumedAt = consumedAt;
    }
    public static PasswordRecovery issue(Long id, Long userId, RecoveryTokenHash hash, Instant now) {
        return new PasswordRecovery(id, userId, hash, now, now.plus(Duration.ofMinutes(30)), null);
    }
    public static PasswordRecovery reconstitute(Long id, Long userId, RecoveryTokenHash hash,
            Instant issuedAt, Instant expiresAt, Instant consumedAt) {
        return new PasswordRecovery(Objects.requireNonNull(id), userId, hash, issuedAt, expiresAt, consumedAt);
    }
    public boolean canReissueAt(Instant now) { return !now.isBefore(issuedAt.plusSeconds(60)); }
    public boolean canConsume(RecoveryTokenHash hash, Instant now) {
        return tokenHash.equals(hash) && consumedAt == null && !now.isBefore(issuedAt) && now.isBefore(expiresAt);
    }
    public void consume(RecoveryTokenHash hash, Instant now) {
        if (!canConsume(hash, now)) throw new IllegalStateException("Invalid recovery grant");
        consumedAt = now;
    }
}

package com.maquigest.platform.iam.domain.model.aggregates;
import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class PasswordRecoveryTests {
    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private final RecoveryTokenHash hash = new RecoveryTokenHash("a".repeat(64));
    @Test void validTokenIsConsumedOnlyOnce() {
        var grant = PasswordRecovery.issue(null, 1L, hash, now);
        assertTrue(grant.canConsume(hash, now));
        grant.consume(hash, now.plusSeconds(1));
        assertFalse(grant.canConsume(hash, now.plusSeconds(2)));
        assertThrows(IllegalStateException.class, () -> grant.consume(hash, now.plusSeconds(2)));
    }
    @Test void expiryAndWrongHashAreRejected() {
        var grant = PasswordRecovery.issue(null, 1L, hash, now);
        assertFalse(grant.canConsume(hash, now.plusSeconds(1800)));
        assertFalse(grant.canConsume(new RecoveryTokenHash("b".repeat(64)), now));
        assertFalse(grant.canConsume(hash, now.minusSeconds(1)));
    }
    @Test void issuanceCooldownIsOneMinute() {
        var grant = PasswordRecovery.issue(null, 1L, hash, now);
        assertFalse(grant.canReissueAt(now.plusSeconds(59)));
        assertTrue(grant.canReissueAt(now.plusSeconds(60)));
    }
}

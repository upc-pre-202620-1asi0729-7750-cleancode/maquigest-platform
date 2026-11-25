package com.maquigest.platform.iam.domain.model.aggregates;
import com.maquigest.platform.iam.domain.model.valueobjects.VerificationTokenHash;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class EmailVerificationTests {
    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");
    private final VerificationTokenHash hash = new VerificationTokenHash("a".repeat(64));
    @Test void validTokenIsConsumedOnlyOnce() {
        var grant = EmailVerification.issue(null, 1L, hash, now);
        assertTrue(grant.canConsume(hash, now));
        grant.consume(hash, now.plusSeconds(1));
        assertFalse(grant.canConsume(hash, now.plusSeconds(2)));
        assertThrows(IllegalStateException.class, () -> grant.consume(hash, now.plusSeconds(2)));
    }
    @Test void expiryAndWrongHashAreRejected() {
        var grant = EmailVerification.issue(null, 1L, hash, now);
        assertFalse(grant.canConsume(hash, now.plusSeconds(86400)));
        assertFalse(grant.canConsume(new VerificationTokenHash("b".repeat(64)), now));
        assertFalse(grant.canConsume(hash, now.minusSeconds(1)));
    }
    @Test void issuanceCooldownIsOneMinute() {
        var grant = EmailVerification.issue(null, 1L, hash, now);
        assertFalse(grant.canReissueAt(now.plusSeconds(59)));
        assertTrue(grant.canReissueAt(now.plusSeconds(60)));
    }
}

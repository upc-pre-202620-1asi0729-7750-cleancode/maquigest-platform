package com.maquigest.platform.subscriptions.domain.model.aggregates;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class UserSubscriptionTests {
    private final Instant start = Instant.parse("2026-11-01T00:00:00Z");
    private final Instant end = Instant.parse("2026-12-01T00:00:00Z");
    @Test void validatesActivePeriodUsingExactInstants() {
        var sub = new UserSubscription(3L,1L,2L,start,end,SubscriptionStatus.ACTIVE,true);
        assertTrue(sub.isActiveAt(start));
        assertTrue(sub.isActiveAt(end));
        assertFalse(sub.isActiveAt(start.minusSeconds(1)));
        assertFalse(sub.isActiveAt(end.plusSeconds(1)));
    }
    @Test void cannotCreateAnInvertedPeriod() {
        assertThrows(IllegalArgumentException.class,()->
            new UserSubscription(null,1L,2L,end,start,SubscriptionStatus.ACTIVE,true));
    }
    @Test void cancelledSubscriptionIsNeverActive() {
        var sub = new UserSubscription(3L,1L,2L,start,end,SubscriptionStatus.ACTIVE,true);
        sub.cancel();
        assertEquals(SubscriptionStatus.CANCELLED,sub.getStatus());
        assertFalse(sub.isActiveAt(start));
        assertThrows(IllegalStateException.class,sub::cancel);
    }
}

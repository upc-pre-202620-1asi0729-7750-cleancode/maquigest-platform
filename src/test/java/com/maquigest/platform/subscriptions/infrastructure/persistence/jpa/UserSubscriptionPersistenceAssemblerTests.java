package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.assemblers.UserSubscriptionPersistenceAssembler;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
class UserSubscriptionPersistenceAssemblerTests {
    @Test void preservesIdsPeriodStatusAndRenewal() {
        Instant start=Instant.parse("2026-11-04T07:00:00Z");
        Instant end=start.plusSeconds(86400L*30);
        var original=new UserSubscription(5L,1L,3L,start,end,SubscriptionStatus.ACTIVE,true);
        var roundtrip=UserSubscriptionPersistenceAssembler.toDomain(
            UserSubscriptionPersistenceAssembler.toPersistence(original));
        assertEquals(5L,roundtrip.getId());
        assertEquals(1L,roundtrip.getUserId());
        assertEquals(3L,roundtrip.getPlanId());
        assertEquals(start,roundtrip.getStartDate());
        assertEquals(end,roundtrip.getEndDate());
        assertTrue(roundtrip.isAutoRenew());
    }
}

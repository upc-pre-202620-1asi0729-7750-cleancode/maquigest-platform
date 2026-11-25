package com.maquigest.platform.subscriptions.application.internal.queryservices;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllUserSubscriptionsQuery;
import com.maquigest.platform.subscriptions.domain.model.queries.GetActiveProviderUserIdsQuery;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UserSubscriptionQueryServiceTests {
    @Test void marketplaceReturnsDistinctActiveProviderIdsAcrossCompanies() {
        var repo = mock(UserSubscriptionRepository.class);
        var now = Instant.now();
        var start = now.minusSeconds(3600);
        var end = now.plusSeconds(3600);
        when(repo.findAll()).thenReturn(List.of(
            new UserSubscription(1L, 11L, 1L, start, end, SubscriptionStatus.ACTIVE, false),
            new UserSubscription(2L, 22L, 2L, start, end, SubscriptionStatus.ACTIVE, true),
            new UserSubscription(3L, 11L, 3L, start, end, SubscriptionStatus.ACTIVE, true),
            new UserSubscription(4L, 33L, 1L, start, end, SubscriptionStatus.CANCELLED, false),
            new UserSubscription(5L, 44L, 1L, start, now.minusSeconds(60), SubscriptionStatus.ACTIVE, false),
            new UserSubscription(6L, 55L, 1L, now.plusSeconds(60), end, SubscriptionStatus.ACTIVE, false),
            new UserSubscription(7L, 66L, 1L, start, end, SubscriptionStatus.EXPIRED, false)
        ));
        var service = new UserSubscriptionQueryServiceImpl(repo);
        assertEquals(List.of(11L, 22L), service.handle(new GetActiveProviderUserIdsQuery()));
    }

    @Test void marketplaceReturnsEmptyListWhenNoSubscriptionsExist() {
        var repo = mock(UserSubscriptionRepository.class);
        when(repo.findAll()).thenReturn(List.of());
        var service = new UserSubscriptionQueryServiceImpl(repo);
        assertTrue(service.handle(new GetActiveProviderUserIdsQuery()).isEmpty());
    }

    @Test void endedSubscriptionIsProjectedAsExpiredWithoutChangingHistory() {
        var repo = mock(UserSubscriptionRepository.class);
        var today = Instant.now();
        var old = new UserSubscription(3L,1L,2L,today.minusSeconds(86400*40L),
            today.minusSeconds(86400),SubscriptionStatus.ACTIVE,true);
        when(repo.findAll()).thenReturn(List.of(old));
        var service = new UserSubscriptionQueryServiceImpl(repo);
        var result=service.handle(new GetAllUserSubscriptionsQuery());
        assertEquals(SubscriptionStatus.EXPIRED,result.getFirst().getStatus());
        assertEquals(SubscriptionStatus.ACTIVE,old.getStatus());
    }
}

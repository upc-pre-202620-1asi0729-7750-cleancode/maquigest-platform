package com.maquigest.platform.subscriptions.application.internal.queryservices;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllUserSubscriptionsQuery;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UserSubscriptionQueryServiceTests {
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

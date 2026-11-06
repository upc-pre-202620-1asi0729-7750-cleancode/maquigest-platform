package com.maquigest.platform.subscriptions.application.internal.commandservices;

import com.maquigest.platform.subscriptions.application.ports.IamSubscriberPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.commands.ChangeUserSubscriptionPlanCommand;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionPlanRepository;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserSubscriptionPlanLifecycleTests {
    private UserSubscription active() {
        return new UserSubscription(14L,7L,3L,Instant.now().minusSeconds(3600),
            Instant.now().plusSeconds(86400), SubscriptionStatus.ACTIVE,true);
    }

    @Test void changingPlanPreservesSubscriptionIdentityAndPeriod() {
        var subscription = active();
        var start = subscription.getStartDate();
        var end = subscription.getEndDate();
        subscription.changePlan(2L);
        assertEquals(14L,subscription.getId());
        assertEquals(2L,subscription.getPlanId());
        assertEquals(start,subscription.getStartDate());
        assertEquals(end,subscription.getEndDate());
        assertEquals(SubscriptionStatus.ACTIVE,subscription.getStatus());
    }

    @Test void cancelledSubscriptionCannotChangePlan() {
        var subscription = active();
        subscription.cancel();
        assertThrows(IllegalStateException.class, () -> subscription.changePlan(2L));
    }

    @Test void commandServiceUpdatesOnlyOwnedActiveSubscription() {
        var subscriptions = mock(UserSubscriptionRepository.class);
        var plans = mock(SubscriptionPlanRepository.class);
        var iam = mock(IamSubscriberPort.class);
        when(iam.lockActiveRentalCompany(7L)).thenReturn(true);
        when(subscriptions.findById(14L)).thenReturn(Optional.of(active()));
        when(plans.findById(2L)).thenReturn(Optional.of(new SubscriptionPlan(2L,"Professional",
            "Professional plan",new BigDecimal("149.00"),"PEN","MONTHLY",PlanStatus.ACTIVE)));
        when(subscriptions.save(any(UserSubscription.class))).thenAnswer(inv -> inv.getArgument(0));
        var commands = new UserSubscriptionCommandServiceImpl(subscriptions,plans,iam);
        commands.handle(new ChangeUserSubscriptionPlanCommand(14L,2L,7L));
        verify(subscriptions).save(argThat(s -> s.getId().equals(14L) && s.getPlanId().equals(2L)));
    }

    @Test void anotherCompanyCannotChangeSubscriptionPlan() {
        var subscriptions = mock(UserSubscriptionRepository.class);
        var plans = mock(SubscriptionPlanRepository.class);
        var iam = mock(IamSubscriberPort.class);
        when(iam.lockActiveRentalCompany(8L)).thenReturn(true);
        when(subscriptions.findById(14L)).thenReturn(Optional.of(active()));
        var commands = new UserSubscriptionCommandServiceImpl(subscriptions,plans,iam);
        commands.handle(new ChangeUserSubscriptionPlanCommand(14L,2L,8L));
        verify(subscriptions,never()).save(any());
        verifyNoInteractions(plans);
    }
}

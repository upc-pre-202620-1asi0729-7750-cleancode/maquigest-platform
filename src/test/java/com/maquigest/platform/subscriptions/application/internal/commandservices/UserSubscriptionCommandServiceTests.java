package com.maquigest.platform.subscriptions.application.internal.commandservices;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.subscriptions.application.ports.IamSubscriberPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.*;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import com.maquigest.platform.subscriptions.domain.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class UserSubscriptionCommandServiceTests {
    UserSubscriptionRepository subs;
    SubscriptionPlanRepository plans;
    IamSubscriberPort iam;
    UserSubscriptionCommandServiceImpl service;
    @BeforeEach void setup() {
        subs=mock(UserSubscriptionRepository.class);
        plans=mock(SubscriptionPlanRepository.class);
        iam=mock(IamSubscriberPort.class);
        service=new UserSubscriptionCommandServiceImpl(subs,plans,iam);
        when(iam.lockActiveRentalCompany(1L)).thenReturn(true);
        when(plans.findById(2L)).thenReturn(Optional.of(new SubscriptionPlan(2L,"Professional","Operations",
            new BigDecimal("149.00"),"PEN","MONTHLY",PlanStatus.ACTIVE)));
        when(subs.save(any())).thenAnswer(call -> call.getArgument(0));
    }
    private CreateUserSubscriptionCommand command() {
        Instant start=Instant.now();
        return new CreateUserSubscriptionCommand(1L,2L,start,start.plusSeconds(86400L*30),true);
    }
    @Test void createsForActiveRentalOwnerAndActivePlan() {
        var result=service.handle(command());
        assertInstanceOf(Result.Success.class,result);
        verify(iam).lockActiveRentalCompany(1L);
        verify(subs).save(any());
    }
    @Test void rejectsDuplicateActiveSubscription() {
        when(subs.hasUnexpiredActiveSubscription(eq(1L),any())).thenReturn(true);
        assertInstanceOf(Result.Failure.class,service.handle(command()));
        verify(subs,never()).save(any());
    }
    @Test void rejectsUnapprovedSubscriber() {
        when(iam.lockActiveRentalCompany(1L)).thenReturn(false);
        assertInstanceOf(Result.Failure.class,service.handle(command()));
        verify(subs,never()).save(any());
    }
    @Test void rejectsUnknownPlan() {
        when(plans.findById(2L)).thenReturn(Optional.empty());
        assertInstanceOf(Result.Failure.class,service.handle(command()));
        verify(subs,never()).save(any());
    }
}

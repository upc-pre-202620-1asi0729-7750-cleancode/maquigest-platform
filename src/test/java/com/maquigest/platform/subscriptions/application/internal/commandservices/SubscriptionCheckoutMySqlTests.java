package com.maquigest.platform.subscriptions.application.internal.commandservices;
import com.maquigest.platform.iam.application.commandservices.UserCommandService;
import com.maquigest.platform.iam.domain.model.commands.SignUpCommand;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.subscriptions.application.commandservices.*;
import com.maquigest.platform.subscriptions.application.ports.StripeCheckoutPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import com.maquigest.platform.subscriptions.domain.repositories.SubscriptionPlanRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest(properties={"maquigest.stripe.required=true","maquigest.iam.email-verification-required=false"})
@EnabledIfSystemProperty(named="maquigest.notificationPersistenceTests",matches="true")
class SubscriptionCheckoutMySqlTests {
    @Autowired UserCommandService users;
    @Autowired SubscriptionCheckoutCommandService checkouts;
    @Autowired UserSubscriptionCommandService subscriptions;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean StripeCheckoutPort stripe;
    @BeforeEach void guard(){var db=System.getProperty("maquigest.notificationExpectedDb", "");assertTrue(db.matches("maquigest_ddd_notification_[a-f0-9]{8}"));assertEquals(db,jdbc.queryForObject("SELECT DATABASE()",String.class));when(stripe.isEnabled()).thenReturn(true);}
    private Long user(){var email="stripe-"+UUID.randomUUID()+"@example.com";assertTrue(users.handle(new SignUpCommand("Test","Company",email,"TestPassword123!","Test",Roles.ROLE_RENTAL_COMPANY)).isSuccess());return jdbc.queryForObject("select id from users where email=?",Long.class,email);}
    private Long plan(){return jdbc.queryForObject("select id from subscription_plans where name='Essential'",Long.class);}
    private StripeCheckoutPort.Session session(SubscriptionCheckout c,String status,String payment,long amount){return new StripeCheckoutPort.Session("cs_test_"+c.getReference().replace("-",""),c.getReference(),amount,"pen",status,payment,true,"payment","https://checkout.stripe.com/test");}
    private long count(Long user){return jdbc.queryForObject("select count(*) from user_subscriptions where user_id=?",Long.class,user);}
    @Test void onlyPaidSessionActivatesAndReplayDoesNotDuplicate(){
        Long user=user(),plan=plan();var command=new CreateSubscriptionCheckoutCommand(user,plan);
        var captured=new java.util.concurrent.atomic.AtomicReference<SubscriptionCheckout>();
        when(stripe.create(any())).thenAnswer(i->{SubscriptionCheckout c=i.getArgument(0);captured.set(c);assertEquals(7900,c.getAmountMinor());return session(c,"open","unpaid",7900);});
        var pending=checkouts.handle(command);assertEquals(CheckoutStatus.PENDING,pending.status());assertEquals(0,count(user));
        var c=captured.get();var id=session(c,"open","unpaid",7900).id();
        when(stripe.retrieve(id)).thenReturn(session(c,"open","unpaid",7900));
        assertEquals(pending.reference(),checkouts.handle(command).reference());verify(stripe,times(1)).create(any());
        assertThrows(CheckoutFailure.class,()->checkouts.handle(new RefreshSubscriptionCheckoutCommand(user(),pending.reference())));
        when(stripe.retrieve(id)).thenReturn(session(c,"complete","paid",1));
        assertThrows(CheckoutFailure.class,()->checkouts.handle(new RefreshSubscriptionCheckoutCommand(user,pending.reference())));assertEquals(0,count(user));
        when(stripe.retrieve(id)).thenReturn(session(c,"complete","paid",7900));
        when(stripe.verifyWebhook(any(),any())).thenReturn(Optional.of(id));
        checkouts.handle(new ProcessSubscriptionWebhookCommand(new byte[]{1},"mock"));checkouts.handle(new ProcessSubscriptionWebhookCommand(new byte[]{1},"mock"));
        var paid=checkouts.handle(new RefreshSubscriptionCheckoutCommand(user,pending.reference()));assertEquals(CheckoutStatus.PAID,paid.status());assertNotNull(paid.subscriptionId());assertEquals(1,count(user));
        assertEquals(Boolean.FALSE,jdbc.queryForObject("select auto_renew from user_subscriptions where id=?",Boolean.class,paid.subscriptionId()));
    }
    @Test void directActivationBlockedWhenPaymentRequired(){Long user=user();var now=Instant.now();assertTrue(subscriptions.handle(new CreateUserSubscriptionCommand(user,plan(),now,now.plusSeconds(3600),true)).isFailure());assertEquals(0,count(user));}
}

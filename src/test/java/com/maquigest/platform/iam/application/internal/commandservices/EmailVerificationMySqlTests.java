package com.maquigest.platform.iam.application.internal.commandservices;
import com.maquigest.platform.iam.application.commandservices.UserCommandService;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.shared.application.email.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties="maquigest.iam.email-verification-required=true")
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named="maquigest.notificationPersistenceTests", matches="true")
class EmailVerificationMySqlTests {
    @Autowired UserCommandService users;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired EmailVerificationTransactions transactions;
    @MockitoBean EmailGateway gateway;
    @BeforeEach void guard() {
        var db=System.getProperty("maquigest.notificationExpectedDb", "");
        assertTrue(db.matches("maquigest_ddd_notification_[a-f0-9]{8}"));
        assertEquals(db,jdbc.queryForObject("SELECT DATABASE()",String.class));
        when(gateway.isEnabled()).thenReturn(true);
    }
    private String account() { return "verify-"+UUID.randomUUID()+"@example.com"; }
    private String register(String email) {
        var message=new CompletableFuture<EmailMessage>();
        doAnswer(i->{message.complete(i.getArgument(0));return null;}).when(gateway).send(any(EmailMessage.class));
        assertTrue(users.handle(new SignUpCommand("Test","Company",email,"TestPassword123!","Test",Roles.ROLE_RENTAL_COMPANY)).isSuccess());
        var mail=message.orTimeout(10,TimeUnit.SECONDS).join();
        assertEquals(email,mail.recipient());
        return mail.body().split("#token=")[1].split("\n")[0];
    }
    private static String body(String token,String password) { return "{\"token\":\""+token+"\",\"password\":\""+password+"\"}"; }
    @Test void pendingAccountCannotSignInUntilVerifiedAndTokenCannotReplay() throws Exception {
        var email=account();var token=register(email);
        assertTrue(users.handle(new SignInCommand(email,"TestPassword123!")).isFailure());
        assertEquals(1L,jdbc.queryForObject("select count(*) from email_verifications where token_hash=?",Long.class,EmailVerificationTokens.hash(token).value()));
        mvc.perform(post("/api/v1/authentication/verify-email").contentType(MediaType.APPLICATION_JSON).content(body(token,"WrongPassword!"))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/authentication/verify-email").contentType(MediaType.APPLICATION_JSON).content(body(token,"TestPassword123!"))).andExpect(status().isNoContent());
        assertTrue(users.handle(new SignInCommand(email,"TestPassword123!")).isSuccess());
        mvc.perform(post("/api/v1/authentication/verify-email").contentType(MediaType.APPLICATION_JSON).content(body(token,"TestPassword123!"))).andExpect(status().isBadRequest());
    }
    @Test void cooldownAndGenericResendResponse() throws Exception {
        var email=account();register(email);transactions.resend(email);verify(gateway,times(1)).send(any(EmailMessage.class));
        mvc.perform(post("/api/v1/authentication/resend-email-verification").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"missing-"+UUID.randomUUID()+"@example.com\"}")).andExpect(status().isAccepted()).andExpect(content().string(""));
    }
    @Test void concurrentVerificationOnlySucceedsOnce() throws Exception {
        var token=register(account());var command=new VerifyEmailCommand(token,"TestPassword123!");
        try(var executor=Executors.newFixedThreadPool(2)) {
            var start=new CountDownLatch(1);Callable<Boolean> task=()->{start.await();return transactions.verify(command);};
            var first=executor.submit(task);var second=executor.submit(task);start.countDown();
            assertNotEquals(first.get(15,TimeUnit.SECONDS),second.get(15,TimeUnit.SECONDS));
        }
    }
}

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
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfSystemProperty(named = "maquigest.notificationPersistenceTests", matches = "true")
class PasswordRecoveryMySqlTests {
    @Autowired private UserCommandService users;
    @Autowired private PasswordRecoveryTransactions transactions;
    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private EmailGateway gateway;
    @BeforeEach void guard() {
        var db = System.getProperty("maquigest.notificationExpectedDb", "");
        assertTrue(db.matches("maquigest_ddd_notification_[a-f0-9]{8}"));
        assertEquals(db, jdbc.queryForObject("SELECT DATABASE()", String.class));
        when(gateway.isEnabled()).thenReturn(true);
    }
    private String account() {
        var email = "recovery-" + UUID.randomUUID() + "@example.com";
        assertTrue(users.handle(new SignUpCommand("Test", "Company", email, "OldPassword123!", "Test", Roles.ROLE_RENTAL_COMPANY)).isSuccess());
        return email;
    }
    private static String resetBody(String token) { return "{\"token\":\"" + token + "\",\"password\":\"NewPassword123!\"}"; }
    @Test void publicEndpointsSendAfterPersistenceAndConsumeOnce() throws Exception {
        var email = account();
        var sent = new CompletableFuture<EmailMessage>();
        doAnswer(inv -> { sent.complete(inv.getArgument(0)); return null; }).when(gateway).send(any(EmailMessage.class));
        mvc.perform(post("/api/v1/authentication/password-recovery").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}")).andExpect(status().isAccepted()).andExpect(content().string(""));
        var message = sent.get(10, TimeUnit.SECONDS);
        assertEquals(email, message.recipient());
        var token = message.body().split("#token=")[1].split("\n")[0];
        assertEquals(1L, jdbc.queryForObject("select count(*) from password_recoveries where token_hash = ?", Long.class,
                PasswordRecoveryTokens.hash(token).value()));
        mvc.perform(post("/api/v1/authentication/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(token))).andExpect(status().isNoContent());
        assertTrue(users.handle(new SignInCommand(email, "NewPassword123!")).isSuccess());
        assertTrue(users.handle(new SignInCommand(email, "OldPassword123!")).isFailure());
        mvc.perform(post("/api/v1/authentication/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(resetBody(token))).andExpect(status().isBadRequest());
    }
    @Test void missingAccountHasSamePublicResponseAndCooldownStopsDuplicateIssue() throws Exception {
        mvc.perform(post("/api/v1/authentication/password-recovery").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"missing-" + UUID.randomUUID() + "@example.com\"}")).andExpect(status().isAccepted()).andExpect(content().string(""));
        var email = account();
        assertTrue(transactions.issue(email).isPresent());
        assertTrue(transactions.issue(email).isEmpty());
    }
    @Test void concurrentResetOnlySucceedsOnce() throws Exception {
        var email = account(); var token = transactions.issue(email).orElseThrow().token();
        var command = new ResetPasswordCommand(token, "NewPassword123!");
        try (var executor = Executors.newFixedThreadPool(2)) {
            var start = new CountDownLatch(1);
            Callable<Boolean> reset = () -> { start.await(); return transactions.reset(command); };
            var first = executor.submit(reset); var second = executor.submit(reset); start.countDown();
            assertNotEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
        }
    }
}

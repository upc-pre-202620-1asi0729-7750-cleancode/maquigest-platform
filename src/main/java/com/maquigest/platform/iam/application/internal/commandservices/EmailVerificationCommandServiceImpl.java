package com.maquigest.platform.iam.application.internal.commandservices;
import com.maquigest.platform.iam.application.commandservices.EmailVerificationCommandService;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.shared.application.email.EmailGateway;
import com.maquigest.platform.shared.application.result.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.*;
@Service @Slf4j
public class EmailVerificationCommandServiceImpl implements EmailVerificationCommandService {
    private final EmailVerificationTransactions transactions;
    private final EmailGateway gateway;
    private final ThreadPoolExecutor worker = new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(100));
    public EmailVerificationCommandServiceImpl(EmailVerificationTransactions transactions, EmailGateway gateway) {
        this.transactions = transactions; this.gateway = gateway;
    }
    @Override public Result<Boolean, ApplicationError> handle(VerifyEmailCommand command) {
        if (command != null && EmailVerificationTokens.valid(command.token()) && command.password() != null
            && !command.password().isBlank() && command.password().getBytes(StandardCharsets.UTF_8).length <= 72
            && transactions.verify(command)) return Result.success(true);
        return Result.failure(new ApplicationError("IAM_VERIFICATION_INVALID", "Invalid verification link or password"));
    }
    @Override public Result<Boolean, ApplicationError> handle(ResendEmailVerificationCommand command) {
        if (command == null || command.email() == null || command.email().isBlank() || command.email().length() > 254)
            return Result.failure(new ApplicationError("IAM_VERIFICATION_INVALID", "Invalid verification request"));
        var email = command.email().trim().toLowerCase(Locale.ROOT);
        try { worker.execute(() -> { try { if (gateway.isEnabled()) transactions.resend(email); }
            catch (RuntimeException ex) { log.warn("Email verification resend failed"); } }); }
        catch (RejectedExecutionException ex) { log.warn("Email verification queue unavailable"); }
        return Result.success(true);
    }
    @PreDestroy public void shutdown() { worker.shutdownNow(); }
}

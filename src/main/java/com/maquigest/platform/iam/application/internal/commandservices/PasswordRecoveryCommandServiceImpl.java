package com.maquigest.platform.iam.application.internal.commandservices;
import com.maquigest.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.maquigest.platform.iam.domain.model.commands.*;
import com.maquigest.platform.shared.application.email.*;
import com.maquigest.platform.shared.application.result.*;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.*;

/** A bounded queue decouples the public response from account lookup and email latency. */
@Service @Slf4j
public class PasswordRecoveryCommandServiceImpl implements PasswordRecoveryCommandService {
    private final PasswordRecoveryTransactions transactions;
    private final EmailGateway gateway;
    private final String resetUrl;
    private final ThreadPoolExecutor worker = new ThreadPoolExecutor(2, 2, 0, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100));
    public PasswordRecoveryCommandServiceImpl(PasswordRecoveryTransactions transactions, EmailGateway gateway,
            @Value("${MAQUIGEST_WEBAPP_BASE_URL:http://localhost:4200}") String baseUrl) {
        this.transactions = transactions; this.gateway = gateway;
        var uri = URI.create(baseUrl);
        boolean local = "http".equals(uri.getScheme()) && "localhost".equals(uri.getHost());
        if ((!local && !"https".equals(uri.getScheme())) || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null || !(uri.getPath().isEmpty() || uri.getPath().equals("/")))
            throw new IllegalArgumentException("Invalid MaquiGest WebApp base URL");
        resetUrl = baseUrl.replaceAll("/+$", "") + "/iam/reset-password#token=";
    }
    @Override public Result<Boolean, ApplicationError> handle(RequestPasswordRecoveryCommand command) {
        if (command == null || command.email() == null || command.email().isBlank() || command.email().length() > 254)
            return Result.failure(new ApplicationError("IAM_RECOVERY_INVALID", "Invalid recovery request"));
        var email = command.email().trim().toLowerCase(Locale.ROOT);
        try {
            worker.execute(() -> deliver(email));
        } catch (RejectedExecutionException ex) {
            log.warn("Password recovery queue unavailable");
        }
        return Result.success(true);
    }
    private void deliver(String email) {
        try {
            if (!gateway.isEnabled()) return;
            // The transaction has committed before the transport is called.
            transactions.issue(email).ifPresent(grant -> gateway.send(new EmailMessage(grant.email(),
                    "MaquiGest - Password recovery / Recuperacion de contrasena",
                    "Reset your password / Restablece tu contrasena (30 min):\n" + resetUrl + grant.token()
                            + "\n\nIf you did not request this, ignore this email. / Si no lo solicitaste, ignora este correo.")));
        } catch (RuntimeException ex) { log.warn("Password recovery processing failed"); }
    }
    @Override public Result<Boolean, ApplicationError> handle(ResetPasswordCommand command) {
        if (command == null || !PasswordRecoveryTokens.valid(command.token()) || command.password() == null
                || command.password().isBlank() || command.password().length() < 8
                || command.password().getBytes(StandardCharsets.UTF_8).length > 72)
            return Result.failure(new ApplicationError("IAM_RESET_INVALID", "Invalid or expired recovery link"));
        return transactions.reset(command) ? Result.success(true)
                : Result.failure(new ApplicationError("IAM_RESET_INVALID", "Invalid or expired recovery link"));
    }
    @PreDestroy public void shutdown() { worker.shutdownNow(); }
}

package com.maquigest.platform.iam.application.internal.eventhandlers;
import com.maquigest.platform.iam.application.internal.commandservices.EmailVerificationTransactions.DeliveryRequested;
import com.maquigest.platform.shared.application.email.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.*;
import lombok.extern.slf4j.Slf4j;
import java.net.URI;
@Service @Slf4j
public class EmailVerificationDeliveryHandler {
    private final EmailGateway gateway;
    private final String url;
    public EmailVerificationDeliveryHandler(EmailGateway gateway,
            @Value("${MAQUIGEST_WEBAPP_BASE_URL:http://localhost:4200}") String baseUrl) {
        this.gateway = gateway;
        var uri = URI.create(baseUrl);
        boolean local = "http".equals(uri.getScheme()) && "localhost".equals(uri.getHost());
        if ((!local && !"https".equals(uri.getScheme())) || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null || !(uri.getPath().isEmpty() || uri.getPath().equals("/")))
            throw new IllegalArgumentException("Invalid MaquiGest WebApp base URL");
        url = baseUrl.replaceAll("/+$", "") + "/iam/verify-email#token=";
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DeliveryRequested event) {
        try {
            if (!gateway.isEnabled()) { log.warn("Email verification delivery disabled"); return; }
            gateway.send(new EmailMessage(event.email(), "MaquiGest - Verify email / Verifica tu correo",
                "Verify your email / Verifica tu correo (24 h):\n" + url + event.token()
                + "\nUse the password you chose at registration. If you did not register, ignore this email. / Usa la contrasena que elegiste al registrarte. Si no te registraste, ignora este correo."));
        } catch (RuntimeException ex) { log.warn("Email verification delivery failed"); }
    }
}

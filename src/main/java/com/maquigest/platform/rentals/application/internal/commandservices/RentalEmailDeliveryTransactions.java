package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.application.ports.RentalNotificationRecipientPort;
import com.maquigest.platform.rentals.domain.model.events.RentalNotificationEmailRequestedEvent;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationEmailStatus;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import com.maquigest.platform.shared.application.email.EmailMessage;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RentalEmailDeliveryTransactions {
    private final NotificationRepository repository;
    private final RentalNotificationRecipientPort recipients;
    private final Locale locale;

    public RentalEmailDeliveryTransactions(NotificationRepository repository, RentalNotificationRecipientPort recipients,
            @Value("${maquigest.email.locale:es-PE}") String languageTag) {
        this.repository = repository;
        this.recipients = recipients;
        this.locale = Locale.forLanguageTag(languageTag);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<EmailMessage> prepare(RentalNotificationEmailRequestedEvent event) {
        var notification = repository.findByIdAndRecipientId(event.notificationId(), event.recipientId());
        if (notification.isEmpty() || notification.get().getEmailStatus() != NotificationEmailStatus.PENDING) {
            return Optional.empty();
        }
        var value = notification.get();
        var recipient = recipients.findActiveEmail(event.recipientId().recipientId())
                .orElseThrow(() -> new IllegalStateException("Active notification recipient unavailable"));
        var bundle = ResourceBundle.getBundle("rentals", locale,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        var key = "notification." + value.getType().name().toLowerCase(Locale.ROOT);
        return Optional.of(new EmailMessage(recipient, "MaquiGest - " + bundle.getString(key + ".title"),
                bundle.getString(key + ".message") + "\nMaquiGest #" + value.getReferenceId().referenceId()));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(RentalNotificationEmailRequestedEvent event, boolean accepted) {
        var notification = repository.findByIdAndRecipientIdForUpdate(event.notificationId(), event.recipientId())
                .orElseThrow(() -> new IllegalStateException("Notification missing"));
        if (notification.getEmailStatus() != NotificationEmailStatus.PENDING) return;
        if (accepted) notification.markEmailAsSent();
        else notification.markEmailAsFailed();
        repository.save(notification);
    }
}

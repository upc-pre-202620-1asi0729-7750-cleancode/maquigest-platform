package com.maquigest.platform.rentals.domain.model.aggregates;

import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationTests {
    private static final Instant OCCURRED = Instant.parse("2026-11-24T12:00:00Z");
    private static Notification create(NotificationType type) {
        return new Notification(new CreateNotificationCommand(new NotificationRecipientId(1L),
                new NotificationReferenceId(2L), type, OCCURRED));
    }
    @Test void requestFactsRequireEmailAndHandoverFactsDoNot() {
        for (var type : NotificationType.values()) {
            var notification = create(type);
            assertNull(notification.getId());
            assertFalse(notification.isRead());
            assertEquals(type.requiresEmail() ? NotificationEmailStatus.PENDING :
                    NotificationEmailStatus.NOT_REQUIRED, notification.getEmailStatus());
        }
        assertTrue(NotificationType.RENTAL_REQUEST_CREATED.requiresEmail());
        assertTrue(NotificationType.RENTAL_REQUEST_APPROVED.requiresEmail());
        assertTrue(NotificationType.RENTAL_REQUEST_REJECTED.requiresEmail());
        assertFalse(NotificationType.RENTAL_DELIVERED.requiresEmail());
        assertFalse(NotificationType.RENTAL_RETURNED.requiresEmail());
    }
    @Test void readStateIsIndependentAndIdempotent() {
        var notification = create(NotificationType.RENTAL_REQUEST_APPROVED);
        var firstRead = OCCURRED.plusSeconds(1);
        notification.markAsRead(firstRead);
        notification.markAsRead(firstRead.plusSeconds(1));
        assertEquals(firstRead, notification.getReadAt());
        assertEquals(NotificationEmailStatus.PENDING, notification.getEmailStatus());
        notification.markEmailAsSent();
        assertTrue(notification.isRead());
        assertEquals(NotificationEmailStatus.SENT, notification.getEmailStatus());
    }
    @Test void invalidTransitionsDoNotChangeState() {
        var internal = create(NotificationType.RENTAL_DELIVERED);
        assertThrows(IllegalStateException.class, internal::markEmailAsSent);
        assertThrows(IllegalStateException.class, internal::markEmailAsFailed);
        assertThrows(IllegalArgumentException.class, () -> internal.markAsRead(OCCURRED.minusSeconds(1)));
        assertFalse(internal.isRead());
        var email = create(NotificationType.RENTAL_REQUEST_CREATED);
        email.markEmailAsFailed();
        assertThrows(IllegalStateException.class, email::markEmailAsSent);
        assertEquals(NotificationEmailStatus.FAILED, email.getEmailStatus());
    }
    @Test void reconstructionPreservesStateWithoutRegisteringEvents() {
        var notification = Notification.reconstitute(7L, new NotificationRecipientId(1L),
                new NotificationReferenceId(2L), NotificationType.RENTAL_REQUEST_CREATED,
                OCCURRED, NotificationEmailStatus.SENT, OCCURRED.plusSeconds(3));
        assertEquals(7L, notification.getId());
        assertEquals(NotificationEmailStatus.SENT, notification.getEmailStatus());
        assertTrue(notification.isRead());
        assertTrue(notification.domainEvents().isEmpty());
    }
    @Test void reconstructionRejectsInconsistentEmailState() {
        assertThrows(IllegalArgumentException.class, () -> Notification.reconstitute(7L,
                new NotificationRecipientId(1L), new NotificationReferenceId(2L),
                NotificationType.RENTAL_DELIVERED, OCCURRED, NotificationEmailStatus.PENDING, null));
    }
}

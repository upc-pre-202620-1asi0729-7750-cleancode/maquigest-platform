package com.maquigest.platform.rentals.domain.model.valueobjects;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationValueObjectsTests {
    @Test void validRecipientIdIsAccepted() {
        assertEquals(1L, new NotificationRecipientId(1L).recipientId());
    }
    @Test void invalidRecipientIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationRecipientId(null));
        assertThrows(IllegalArgumentException.class, () -> new NotificationRecipientId(0L));
        assertThrows(IllegalArgumentException.class, () -> new NotificationRecipientId(-1L));
    }
    @Test void validReferenceIdIsAccepted() {
        assertEquals(1L, new NotificationReferenceId(1L).referenceId());
    }
    @Test void invalidReferenceIdsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationReferenceId(null));
        assertThrows(IllegalArgumentException.class, () -> new NotificationReferenceId(0L));
        assertThrows(IllegalArgumentException.class, () -> new NotificationReferenceId(-1L));
    }
}

package com.maquigest.platform.rentals.application.internal.commandservices;

import com.maquigest.platform.rentals.domain.model.aggregates.Notification;
import com.maquigest.platform.rentals.domain.model.commands.CreateNotificationCommand;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.NotificationRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationCommandServiceTests {
    private final CreateNotificationCommand command = new CreateNotificationCommand(
            new NotificationRecipientId(1L), new NotificationReferenceId(2L),
            NotificationType.RENTAL_REQUEST_CREATED, Instant.EPOCH);
    private Notification persisted() {
        return Notification.reconstitute(8L, command.recipientId(), command.referenceId(),
                command.type(), command.occurredAt(), NotificationEmailStatus.PENDING, null);
    }
    @Test void resultContainsThePersistedIdentity() {
        var repository = mock(NotificationRepository.class);
        when(repository.findByRecipientIdAndReferenceIdAndType(command.recipientId(), command.referenceId(),
                command.type())).thenReturn(Optional.empty());
        when(repository.save(any(Notification.class))).thenReturn(persisted());
        var service = new NotificationCommandServiceImpl(new NotificationTransactionalExecutor(repository));
        assertEquals(8L, service.handle(command).toOptional().orElseThrow());
        verify(repository).save(any(Notification.class));
    }
    @Test void repeatedFactReturnsExistingIdentityWithoutAnotherInsert() {
        var repository = mock(NotificationRepository.class);
        when(repository.findByRecipientIdAndReferenceIdAndType(command.recipientId(), command.referenceId(),
                command.type())).thenReturn(Optional.of(persisted()));
        var service = new NotificationCommandServiceImpl(new NotificationTransactionalExecutor(repository));
        assertEquals(8L, service.handle(command).toOptional().orElseThrow());
        verify(repository, never()).save(any());
    }
    @Test void nullCommandIsAnExpectedFailure() {
        var executor = mock(NotificationTransactionalExecutor.class);
        assertTrue(new NotificationCommandServiceImpl(executor).handle((CreateNotificationCommand) null).isFailure());
        verifyNoInteractions(executor);
    }
}

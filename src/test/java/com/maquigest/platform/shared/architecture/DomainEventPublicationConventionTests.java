package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.iam.domain.model.aggregates.EmailVerification;
import com.maquigest.platform.iam.domain.model.aggregates.PasswordRecovery;
import com.maquigest.platform.iam.domain.model.valueobjects.VerificationTokenHash;
import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters.EmailVerificationRepositoryImpl;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters.PasswordRecoveryRepositoryImpl;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.EmailVerificationPersistenceRepository;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.PasswordRecoveryPersistenceRepository;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.adapters.SubscriptionCheckoutRepositoryImpl;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionCheckoutPersistenceRepository;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.time.Instant;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DomainEventPublicationConventionTests {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test void aggregateWriteAdaptersInjectSharedPublisher() throws Exception {
        var root = Path.of("src", "main", "java");
        int count = 0;
        try (var paths = Files.walk(root.resolve("com/maquigest/platform"))) {
            for (var source : paths.filter(p -> p.getFileName().toString().endsWith("RepositoryImpl.java")).toList()) {
                var adapter = Class.forName(root.relativize(source).toString()
                        .replace('\\', '.').replace('/', '.').replaceFirst("\\.java$", ""));
                boolean writesAggregate = Arrays.stream(adapter.getDeclaredMethods())
                        .anyMatch(m -> m.getName().equals("save") && m.getParameterCount() == 1
                                && AbstractDomainAggregateRoot.class.isAssignableFrom(m.getParameterTypes()[0]));
                if (!writesAggregate) continue;
                count++;
                assertTrue(Arrays.stream(adapter.getDeclaredFields()).anyMatch(f -> f.getType() == DomainEventPublisher.class), adapter.getName());
                assertTrue(Arrays.stream(adapter.getConstructors()).anyMatch(c -> Arrays.asList(c.getParameterTypes()).contains(DomainEventPublisher.class)), adapter.getName());
                assertTrue(Files.readString(source).contains(".publishAndClearEvents("), adapter.getName());
            }
        }
        assertEquals(17, count);
    }

    @Test void emailVerificationPublishesOriginalAggregateAfterSave() {
        var persistence = mock(EmailVerificationPersistenceRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var value = EmailVerification.issue(null, 3L, new VerificationTokenHash("a".repeat(64)), NOW);
        when(persistence.saveAndFlush(any())).thenAnswer(invocation -> {
            var entity = invocation.<com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationPersistenceEntity>getArgument(0);
            entity.setId(77L);
            return entity;
        });
        var saved = new EmailVerificationRepositoryImpl(persistence, publisher).save(value);
        assertEquals(77L, saved.getId());
        assertEquals(value.getTokenHash(), saved.getTokenHash());
        assertTrue(value.domainEvents().isEmpty());
        var order = inOrder(persistence, publisher);
        order.verify(persistence).saveAndFlush(any());
        order.verify(publisher).publishAndClearEvents(same(value));
        verifyNoMoreInteractions(publisher);
    }

    @Test void emailVerificationDoesNotPublishOnFailedSave() {
        var persistence = mock(EmailVerificationPersistenceRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var value = EmailVerification.issue(null, 3L, new VerificationTokenHash("a".repeat(64)), NOW);
        var failure = new IllegalStateException("persistence failed");
        when(persistence.saveAndFlush(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new EmailVerificationRepositoryImpl(persistence, publisher).save(value)));
        verifyNoInteractions(publisher);
    }

    @Test void passwordRecoveryPublishesOriginalAggregateAfterSave() {
        var persistence = mock(PasswordRecoveryPersistenceRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var value = PasswordRecovery.issue(null, 3L, new RecoveryTokenHash("b".repeat(64)), NOW);
        when(persistence.saveAndFlush(any())).thenAnswer(invocation -> {
            var entity = invocation.<com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryPersistenceEntity>getArgument(0);
            entity.setId(77L);
            return entity;
        });
        var saved = new PasswordRecoveryRepositoryImpl(persistence, publisher).save(value);
        assertEquals(77L, saved.getId());
        assertEquals(value.getTokenHash(), saved.getTokenHash());
        assertTrue(value.domainEvents().isEmpty());
        var order = inOrder(persistence, publisher);
        order.verify(persistence).saveAndFlush(any());
        order.verify(publisher).publishAndClearEvents(same(value));
        verifyNoMoreInteractions(publisher);
    }

    @Test void passwordRecoveryDoesNotPublishOnFailedSave() {
        var persistence = mock(PasswordRecoveryPersistenceRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var value = PasswordRecovery.issue(null, 3L, new RecoveryTokenHash("b".repeat(64)), NOW);
        var failure = new IllegalStateException("persistence failed");
        when(persistence.saveAndFlush(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new PasswordRecoveryRepositoryImpl(persistence, publisher).save(value)));
        verifyNoInteractions(publisher);
    }

    @Test void subscriptionCheckoutPublishesOriginalAggregateAfterSave() {
        var persistence = mock(SubscriptionCheckoutPersistenceRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var value = SubscriptionCheckout.issue(3L, 1L, 7900, NOW.plusSeconds(3600));
        when(persistence.saveAndFlush(any())).thenAnswer(invocation -> {
            var entity = invocation.<com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionCheckoutPersistenceEntity>getArgument(0);
            entity.setId(77L);
            return entity;
        });
        var saved = new SubscriptionCheckoutRepositoryImpl(persistence, publisher).save(value);
        assertEquals(77L, saved.getId());
        assertEquals(value.getReference(), saved.getReference());
        assertTrue(value.domainEvents().isEmpty());
        var order = inOrder(persistence, publisher);
        order.verify(persistence).saveAndFlush(any());
        order.verify(publisher).publishAndClearEvents(same(value));
        verifyNoMoreInteractions(publisher);
    }

    @Test void subscriptionCheckoutDoesNotPublishOnFailedSave() {
        var persistence = mock(SubscriptionCheckoutPersistenceRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var value = SubscriptionCheckout.issue(3L, 1L, 7900, NOW.plusSeconds(3600));
        var failure = new IllegalStateException("persistence failed");
        when(persistence.saveAndFlush(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new SubscriptionCheckoutRepositoryImpl(persistence, publisher).save(value)));
        verifyNoInteractions(publisher);
    }
}

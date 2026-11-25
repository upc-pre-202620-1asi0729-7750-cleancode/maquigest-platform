package com.maquigest.platform.subscriptions.interfaces.rest;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade.ActorSnapshot;
import com.maquigest.platform.subscriptions.application.commandservices.UserSubscriptionCommandService;
import com.maquigest.platform.subscriptions.application.internal.queryservices.UserSubscriptionQueryServiceImpl;
import com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.domain.repositories.UserSubscriptionRepository;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.UserSubscriptionResource;
import com.maquigest.platform.subscriptions.interfaces.rest.security.SubscriptionOwnershipPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSubscriptionReadOwnershipTests {
    private final IamActorFacade actors = mock(IamActorFacade.class);
    private final UserSubscriptionRepository repository = mock(UserSubscriptionRepository.class);
    private final UserSubscriptionCommandService commands = mock(UserSubscriptionCommandService.class);
    private final UserSubscriptionsController controller = new UserSubscriptionsController(
        new UserSubscriptionQueryServiceImpl(repository), commands, new SubscriptionOwnershipPolicy(actors));

    @BeforeEach void setup() {
        when(actors.findActiveByEmail("rental@example.test"))
            .thenReturn(Optional.of(new ActorSnapshot(10L, true, false, false)));
        when(actors.findActiveByEmail("construction@example.test"))
            .thenReturn(Optional.of(new ActorSnapshot(20L, false, true, false)));
        when(actors.findActiveByEmail("admin@example.test"))
            .thenReturn(Optional.of(new ActorSnapshot(30L, false, false, true)));
        when(actors.findActiveByEmail("inactive@example.test")).thenReturn(Optional.empty());
    }

    @Test void listUsesAuthenticatedOwnerAndPreservesEffectiveStatus() {
        Instant now = Instant.now();
        var expired = new UserSubscription(101L, 10L, 1L, now.minusSeconds(7200),
            now.minusSeconds(3600), SubscriptionStatus.ACTIVE, true);
        when(repository.findAllByUserId(10L)).thenReturn(List.of(expired));
        var response = controller.getAll(auth("rental@example.test"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        var body = assertInstanceOf(List.class, response.getBody());
        assertEquals(1, body.size());
        var resource = assertInstanceOf(UserSubscriptionResource.class, body.getFirst());
        assertEquals(10L, resource.userId());
        assertEquals(101L, resource.id());
        assertEquals(SubscriptionStatus.EXPIRED, resource.status());
        assertEquals(SubscriptionStatus.ACTIVE, expired.getStatus());
        verify(repository).findAllByUserId(10L);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(commands);
    }

    @Test void ownerCanReadOwnSubscription() {
        Instant now = Instant.now();
        var subscription = new UserSubscription(101L, 10L, 1L, now.minusSeconds(60),
            now.plusSeconds(3600), SubscriptionStatus.ACTIVE, true);
        when(repository.findByIdAndUserId(101L, 10L)).thenReturn(Optional.of(subscription));
        var response = controller.getById(101L, auth("rental@example.test"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(101L, assertInstanceOf(UserSubscriptionResource.class, response.getBody()).id());
        verify(repository).findByIdAndUserId(101L, 10L);
        verifyNoMoreInteractions(repository);
    }

    @Test void foreignAndMissingSubscriptionsBothReturn404() {
        when(repository.findByIdAndUserId(202L, 10L)).thenReturn(Optional.empty());
        when(repository.findByIdAndUserId(999L, 10L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.getById(202L, auth("rental@example.test")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getById(999L, auth("rental@example.test")).getStatusCode());
        verify(repository).findByIdAndUserId(202L, 10L);
        verify(repository).findByIdAndUserId(999L, 10L);
        verifyNoMoreInteractions(repository);
    }

    @Test void constructionAccountWithoutSubscriptionsGetsEmptyList() {
        when(repository.findAllByUserId(20L)).thenReturn(List.of());
        var response = controller.getAll(auth("construction@example.test"));
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(), response.getBody());
        verify(repository).findAllByUserId(20L);
        verifyNoMoreInteractions(repository);
    }

    @Test void administratorReadIsAlsoScopedToOwnAccount() {
        when(repository.findAllByUserId(30L)).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.getAll(auth("admin@example.test")).getStatusCode());
        verify(repository).findAllByUserId(30L);
        verifyNoMoreInteractions(repository);
    }

    @Test void unauthenticatedOrInactiveAccountCannotRead() {
        assertEquals(HttpStatus.FORBIDDEN, controller.getAll(null).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, controller.getById(101L, auth("inactive@example.test")).getStatusCode());
        verifyNoInteractions(repository, commands);
    }

    private static UsernamePasswordAuthenticationToken auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }
}

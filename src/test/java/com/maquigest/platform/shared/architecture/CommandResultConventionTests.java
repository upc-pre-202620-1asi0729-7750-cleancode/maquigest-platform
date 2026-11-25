package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.iam.application.internal.commandservices.RoleCommandServiceImpl;
import com.maquigest.platform.iam.domain.model.commands.SeedRolesCommand;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.repositories.RoleRepository;
import com.maquigest.platform.shared.application.result.*;
import com.maquigest.platform.subscriptions.application.commandservices.*;
import com.maquigest.platform.subscriptions.application.commandservices.SubscriptionCheckoutCommandService.CheckoutResult;
import com.maquigest.platform.subscriptions.application.internal.commandservices.*;
import com.maquigest.platform.subscriptions.application.ports.StripeCheckoutPort;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus;
import com.maquigest.platform.subscriptions.interfaces.rest.SubscriptionCheckoutsController;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.*;
import com.maquigest.platform.subscriptions.interfaces.rest.security.SubscriptionOwnershipPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.ParameterizedType;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandResultConventionTests {
    @Test void allCommandContractsReturnTypedResults() throws Exception {
        var root = Path.of("src", "main", "java");
        try (var paths = Files.walk(root.resolve("com/maquigest/platform"))) {
            var services = paths.filter(p -> p.getFileName().toString().endsWith("CommandService.java"))
                .filter(p -> p.getParent().getFileName().toString().equals("commandservices")).toList();
            assertEquals(19, services.size());
            for (var source : services) {
                var service = Class.forName(root.relativize(source).toString().replace('\\', '.').replace('/', '.').replaceFirst("\\.java$", ""));
                for (var method : service.getDeclaredMethods()) {
                    assertEquals(Result.class, method.getReturnType(), method.toString());
                    var type = assertInstanceOf(ParameterizedType.class, method.getGenericReturnType());
                    assertEquals(ApplicationError.class, type.getActualTypeArguments()[1]);
                }
            }
        }
    }

    @Test void roleSeedReturnsCreatedCountAndRemainsIdempotent() {
        var repository = mock(RoleRepository.class);
        var existing = EnumSet.noneOf(Roles.class);
        when(repository.existsByName(any())).thenAnswer(i -> existing.contains(i.getArgument(0)));
        when(repository.save(any())).thenAnswer(i -> {
            com.maquigest.platform.iam.domain.model.entities.Role role = i.getArgument(0);
            existing.add(role.getName()); return role;
        });
        var service = new RoleCommandServiceImpl(repository);
        assertEquals(Roles.values().length, service.handle(new SeedRolesCommand()).toOptional().orElseThrow());
        assertEquals(0, service.handle(new SeedRolesCommand()).toOptional().orElseThrow());
        verify(repository, times(Roles.values().length)).save(any());
    }

    @Test void seedDoesNotHideUnexpectedPersistenceFailure() {
        var repository = mock(RoleRepository.class);
        var failure = new IllegalStateException("persistence failed");
        when(repository.save(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
            () -> new RoleCommandServiceImpl(repository).handle(new SeedRolesCommand())));
    }

    private String code(Result<?, ApplicationError> result) {
        return assertInstanceOf(ApplicationError.class, assertInstanceOf(Result.Failure.class, result).error()).code();
    }

    @Test void checkoutAndRefreshReturnSuccessWithOriginalProjection() {
        var stripe = mock(StripeCheckoutPort.class);
        var tx = mock(SubscriptionCheckoutTransactions.class);
        var service = new SubscriptionCheckoutCommandServiceImpl(tx, stripe);
        var checkout = SubscriptionCheckout.issue(3L, 1L, 7900, Instant.now().plusSeconds(7200));
        var session = new StripeCheckoutPort.Session("cs_test_a", checkout.getReference(), 7900, "pen", "open", "unpaid", true, "payment", "https://checkout.stripe.com/test");
        when(stripe.isEnabled()).thenReturn(true);
        when(tx.reserve(3L, 1L)).thenReturn(checkout);
        when(stripe.create(checkout)).thenReturn(session);
        when(tx.reconcile(3L, session)).thenAnswer(i -> {checkout.bindSession(session.id()); return checkout;});
        when(tx.find(checkout.getReference())).thenReturn(checkout);
        when(stripe.retrieve(session.id())).thenReturn(session);
        var result = service.handle(new CreateSubscriptionCheckoutCommand(3L, 1L)).toOptional().orElseThrow();
        assertEquals(new CheckoutResult(checkout.getReference(), CheckoutStatus.PENDING, session.url(), null), result);
        var refreshed = service.handle(new RefreshSubscriptionCheckoutCommand(3L, checkout.getReference())).toOptional().orElseThrow();
        assertEquals(new CheckoutResult(checkout.getReference(), CheckoutStatus.PENDING, null, null), refreshed);
        verify(stripe, times(1)).create(checkout);
    }

    @Test void checkoutExpectedFailuresAreTypedAcrossAllThreeCommands() {
        for (var value : CheckoutFailure.Code.values()) {
            var stripe = mock(StripeCheckoutPort.class);
            var tx = mock(SubscriptionCheckoutTransactions.class);
            var service = new SubscriptionCheckoutCommandServiceImpl(tx, stripe);
            when(stripe.isEnabled()).thenReturn(true);
            when(tx.reserve(3L, 1L)).thenThrow(new CheckoutFailure(value));
            when(tx.find("ref")).thenThrow(new CheckoutFailure(value));
            when(stripe.verifyWebhook(any(byte[].class), eq("sig"))).thenThrow(new CheckoutFailure(value));
            assertEquals("CHECKOUT_" + value.name(), code(service.handle(new CreateSubscriptionCheckoutCommand(3L, 1L))));
            assertEquals("CHECKOUT_" + value.name(), code(service.handle(new RefreshSubscriptionCheckoutCommand(3L, "ref"))));
            assertEquals("CHECKOUT_" + value.name(), code(service.handle(new ProcessSubscriptionWebhookCommand(new byte[]{1}, "sig"))));
        }
    }

    @Test void ignoredWebhookIsStillSuccessfulAndDoesNotReconcile() {
        var stripe = mock(StripeCheckoutPort.class);
        var tx = mock(SubscriptionCheckoutTransactions.class);
        when(stripe.isEnabled()).thenReturn(true);
        when(stripe.verifyWebhook(any(byte[].class), eq("sig"))).thenReturn(Optional.empty());
        var service = new SubscriptionCheckoutCommandServiceImpl(tx, stripe);
        assertEquals(Boolean.TRUE, service.handle(new ProcessSubscriptionWebhookCommand(new byte[]{1}, "sig")).toOptional().orElseThrow());
        verifyNoInteractions(tx);
    }

    @Test void checkoutUnexpectedFailureEscapesNonTransactionalWrapper() {
        var stripe = mock(StripeCheckoutPort.class);
        var tx = mock(SubscriptionCheckoutTransactions.class);
        var failure = new IllegalStateException("database unavailable");
        when(stripe.isEnabled()).thenReturn(true);
        when(tx.reserve(3L, 1L)).thenThrow(failure);
        var service = new SubscriptionCheckoutCommandServiceImpl(tx, stripe);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.handle(new CreateSubscriptionCheckoutCommand(3L, 1L))));
        assertFalse(SubscriptionCheckoutCommandServiceImpl.class.isAnnotationPresent(Transactional.class));
        for (var method : SubscriptionCheckoutCommandServiceImpl.class.getDeclaredMethods())
            assertFalse(method.isAnnotationPresent(Transactional.class));
    }

    @Test void controllerKeepsSuccessAndLocalizedCheckoutErrorContract() {
        var commands = mock(SubscriptionCheckoutCommandService.class);
        var ownership = mock(SubscriptionOwnershipPolicy.class);
        var auth = mock(Authentication.class);
        when(ownership.authenticatedOwnerId(auth)).thenReturn(Optional.of(3L));
        var messages = new StaticMessageSource();
        var locale = LocaleContextHolder.getLocale();
        var controller = new SubscriptionCheckoutsController(commands, ownership, messages);
        var success = new CheckoutResult("ref", CheckoutStatus.PENDING, "https://checkout.stripe.com/test", null);
        when(commands.handle(any(CreateSubscriptionCheckoutCommand.class))).thenReturn(Result.success(success));
        when(commands.handle(any(RefreshSubscriptionCheckoutCommand.class))).thenReturn(Result.success(success));
        when(commands.handle(any(ProcessSubscriptionWebhookCommand.class))).thenReturn(Result.success(true));
        assertEquals(200, controller.create(new CreateSubscriptionCheckoutResource(1L), auth).getStatusCode().value());
        assertEquals(200, controller.confirm("ref", auth).getStatusCode().value());
        assertEquals(204, controller.webhook(new byte[]{1}, "sig").getStatusCode().value());
        for (var value : CheckoutFailure.Code.values()) {
            String detail = "localized " + value.name();
            messages.addMessage("subscriptions.checkout." + value.name().toLowerCase(Locale.ROOT), locale, detail);
            var error = new CheckoutFailure(value).toApplicationError();
            when(commands.handle(any(ProcessSubscriptionWebhookCommand.class))).thenReturn(Result.failure(error));
            var response = controller.error(assertThrows(CheckoutFailure.class, () -> controller.webhook(new byte[]{1}, "sig")));
            int expected = switch(value) {case UNAVAILABLE, PROVIDER -> 503; case FORBIDDEN -> 403; case NOT_FOUND -> 404; case CONFLICT -> 409; case INVALID -> 400;};
            assertEquals(expected, response.getStatusCode().value());
            assertEquals(detail, response.getBody().getDetail());
            assertEquals(error.code(), response.getBody().getProperties().get("code"));
        }
    }
}

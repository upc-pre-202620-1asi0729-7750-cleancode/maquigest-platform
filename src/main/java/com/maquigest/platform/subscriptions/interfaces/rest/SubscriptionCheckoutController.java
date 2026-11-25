package com.maquigest.platform.subscriptions.interfaces.rest;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;

import com.maquigest.platform.subscriptions.application.commandservices.*;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.CreateSubscriptionCheckoutResource;
import com.maquigest.platform.subscriptions.interfaces.rest.security.SubscriptionOwnershipPolicy;
import com.maquigest.platform.subscriptions.interfaces.rest.transform.SubscriptionCheckoutResourceAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import static com.maquigest.platform.subscriptions.application.commandservices.CheckoutFailure.Code.FORBIDDEN;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController
@RequestMapping("/api/v1/subscription-checkouts")
@Tag(name="Subscription Checkouts")
public class SubscriptionCheckoutController {
    private final SubscriptionCheckoutCommandService commands;
    private final SubscriptionOwnershipPolicy ownership;
    private final MessageSource messages;
    public SubscriptionCheckoutController(SubscriptionCheckoutCommandService commands,SubscriptionOwnershipPolicy ownership,MessageSource messages){this.commands=commands;this.ownership=ownership;this.messages=messages;}
    private Long owner(Authentication auth){return ownership.authenticatedOwnerId(auth).orElseThrow(()->new CheckoutFailure(FORBIDDEN));}
    @PostMapping @SecurityRequirement(name="bearerAuth") @Operation(summary="Start or resume a Stripe test checkout")
    public ResponseEntity<?> create(@Valid @RequestBody CreateSubscriptionCheckoutResource resource,Authentication auth){
        return ResponseEntity.ok(SubscriptionCheckoutResourceAssembler.toResource(ResponseEntityAssembler.valueOrThrow(
            commands.handle(new CreateSubscriptionCheckoutCommand(owner(auth),resource.planId())), CheckoutFailure::fromApplicationError)));
    }
    @PostMapping("/{reference}/confirm") @SecurityRequirement(name="bearerAuth") @Operation(summary="Reconcile payment with Stripe, not with browser claims")
    public ResponseEntity<?> confirm(@PathVariable String reference,Authentication auth){
        return ResponseEntity.ok(SubscriptionCheckoutResourceAssembler.toResource(ResponseEntityAssembler.valueOrThrow(
            commands.handle(new RefreshSubscriptionCheckoutCommand(owner(auth),reference)), CheckoutFailure::fromApplicationError)));
    }
    @PostMapping(value="/webhook",consumes=MediaType.APPLICATION_JSON_VALUE) @Operation(summary="Receive signed Stripe checkout events")
    public ResponseEntity<Void> webhook(@RequestBody byte[] payload,@RequestHeader(value="Stripe-Signature",required=false) String signature){
        ResponseEntityAssembler.valueOrThrow(commands.handle(new ProcessSubscriptionWebhookCommand(payload,signature)), CheckoutFailure::fromApplicationError);
        return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(CheckoutFailure.class) public ResponseEntity<ProblemDetail> error(CheckoutFailure e){
        var code = e.toApplicationError().code();
        var status = ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(code);
        var detail = messages.getMessage("subscriptions.checkout."+e.code().name().toLowerCase(java.util.Locale.ROOT),null,LocaleContextHolder.getLocale());
        return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                new ApplicationError(code, detail), HttpStatus.valueOf(status.value()).getReasonPhrase());
    }
}

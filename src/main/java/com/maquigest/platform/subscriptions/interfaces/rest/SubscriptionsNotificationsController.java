package com.maquigest.platform.subscriptions.interfaces.rest;

import com.maquigest.platform.subscriptions.application.commandservices.NotificationCommandService;
import com.maquigest.platform.subscriptions.application.queryservices.NotificationQueryService;
import com.maquigest.platform.subscriptions.domain.model.commands.MarkNotificationAsReadCommand;
import com.maquigest.platform.subscriptions.domain.model.queries.*;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.subscriptions.interfaces.rest.transform.NotificationResourceAssembler;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Locale;
import java.util.Optional;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/subscriptions-notifications", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions Notifications")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionsNotificationsController {
    private final NotificationQueryService queries;
    private final NotificationCommandService commands;
    private final IamActorFacade actors;
    public SubscriptionsNotificationsController(NotificationQueryService queries, NotificationCommandService commands, IamActorFacade actors) {
        this.queries = queries; this.commands = commands; this.actors = actors;
    }
    private Optional<NotificationRecipientId> recipient(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return Optional.empty();
        return actors.findActiveByEmail(auth.getName()).map(actor -> new NotificationRecipientId(actor.userId()));
    }
    private ResponseEntity<?> forbidden() { return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); }
    private ResponseEntity<?> invalid() { return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(ApplicationError.validationError("id", "Positive id required")); }
    @GetMapping
    @Operation(summary = "List notifications owned by the authenticated recipient")
    public ResponseEntity<?> getAll(Authentication auth, Locale locale) {
        var recipient = recipient(auth);
        if (recipient.isEmpty()) return forbidden();
        return ResponseEntity.ok(queries.handle(new GetNotificationsQuery(recipient.get())).stream()
                .map(n -> NotificationResourceAssembler.from(n, locale)).toList());
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get an owned notification")
    public ResponseEntity<?> getById(@PathVariable Long id, Authentication auth, Locale locale) {
        var recipient = recipient(auth);
        if (recipient.isEmpty()) return forbidden();
        if (id == null || id < 1) return invalid();
        var result = queries.handle(new GetNotificationByIdQuery(id, recipient.get()));
        if (result.isEmpty()) return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(ApplicationError.notFound("Notification", id.toString()));
        return ResponseEntity.ok(NotificationResourceAssembler.from(result.get(), locale));
    }
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark an owned notification as read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication auth, Locale locale) {
        var recipient = recipient(auth);
        if (recipient.isEmpty()) return forbidden();
        if (id == null || id < 1) return invalid();
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new MarkNotificationAsReadCommand(id, recipient.get())),
                n -> NotificationResourceAssembler.from(n, locale), HttpStatus.OK);
    }
}

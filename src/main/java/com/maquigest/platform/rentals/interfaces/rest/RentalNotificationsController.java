package com.maquigest.platform.rentals.interfaces.rest;

import com.maquigest.platform.rentals.application.commandservices.NotificationCommandService;
import com.maquigest.platform.rentals.application.queryservices.NotificationQueryService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.commands.MarkNotificationAsReadCommand;
import com.maquigest.platform.rentals.domain.model.queries.GetNotificationsQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetNotificationByIdQuery;
import com.maquigest.platform.rentals.domain.model.valueobjects.NotificationRecipientId;
import com.maquigest.platform.rentals.interfaces.rest.resources.NotificationResource;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.rentals.interfaces.rest.transform.NotificationResourceAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/rental-notifications", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Rental Notifications", description = "Notifications owned by the authenticated rental participant")
@SecurityRequirement(name = "bearerAuth")
public class RentalNotificationsController {
    private final NotificationQueryService queries;
    private final NotificationCommandService commands;
    private final RentalActorPolicy policy;

    public RentalNotificationsController(NotificationQueryService queries, NotificationCommandService commands,
                                         RentalActorPolicy policy) {
        this.queries = queries;
        this.commands = commands;
        this.policy = policy;
    }

    private NotificationRecipientId recipient(Authentication auth) {
        return new NotificationRecipientId(policy.current(auth).orElseThrow(RentalsException::forbidden).userId());
    }

    private void requireId(Long id) {
        if (id == null || id < 1) throw RentalsException.invalid("Notification id must be positive");
    }

    @GetMapping
    @Operation(summary = "Get all notifications owned by the authenticated recipient")
    public List<NotificationResource> getAllNotifications(Authentication auth, Locale locale) {
        return queries.handle(new GetNotificationsQuery(recipient(auth))).stream()
                .map(notification -> NotificationResourceAssembler.from(notification, locale)).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one notification owned by the authenticated recipient")
    public NotificationResource getNotificationById(@PathVariable Long id, Authentication auth, Locale locale) {
        requireId(id);
        return queries.handle(new GetNotificationByIdQuery(id, recipient(auth)))
                .map(notification -> NotificationResourceAssembler.from(notification, locale))
                .orElseThrow(() -> RentalsException.notFound("Notification"));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark an owned notification as read; repeated requests are idempotent")
    public NotificationResource markNotificationAsRead(@PathVariable Long id, Authentication auth, Locale locale) {
        requireId(id);
        var result = commands.handle(new MarkNotificationAsReadCommand(id, recipient(auth)));
        var notification = ResponseEntityAssembler.valueOrThrow(result, RentalsException::fromApplicationError);
        return NotificationResourceAssembler.from(notification, locale);
    }
}

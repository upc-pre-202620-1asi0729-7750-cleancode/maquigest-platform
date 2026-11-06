package com.maquigest.platform.subscriptions.interfaces.rest;
import com.maquigest.platform.shared.application.result.*;
import com.maquigest.platform.shared.interfaces.rest.transform.*;
import com.maquigest.platform.subscriptions.application.commandservices.UserSubscriptionCommandService;
import com.maquigest.platform.subscriptions.application.queryservices.UserSubscriptionQueryService;
import com.maquigest.platform.subscriptions.domain.model.commands.*;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllUserSubscriptionsQuery;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.maquigest.platform.subscriptions.interfaces.rest.security.SubscriptionOwnershipPolicy;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.CreateUserSubscriptionResource;
import com.maquigest.platform.subscriptions.interfaces.rest.resources.UserSubscriptionResource;
import com.maquigest.platform.subscriptions.interfaces.rest.transform.UserSubscriptionResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value="/api/v1/user-subscriptions",produces=APPLICATION_JSON_VALUE)
@Tag(name="User Subscriptions",description="Subscribe rental companies and control inventory access")
@SecurityRequirement(name="bearerAuth")
public class UserSubscriptionController {
    private final UserSubscriptionQueryService queries;
    private final UserSubscriptionCommandService commands;
    private final SubscriptionOwnershipPolicy ownership;
    public UserSubscriptionController(UserSubscriptionQueryService queries,
            UserSubscriptionCommandService commands, SubscriptionOwnershipPolicy ownership) {
        this.queries=queries; this.commands=commands; this.ownership=ownership;
    }
    @GetMapping @Operation(summary="List subscriptions consumed by WebApp and Inventory ACL")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(queries.handle(new GetAllUserSubscriptionsQuery()).stream()
            .map(UserSubscriptionResourceFromEntityAssembler::toResourceFromEntity).toList());
    }
    @GetMapping("/{id}") @Operation(summary="Find a subscription by id")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return queries.findById(id)
            .<ResponseEntity<?>>map(s -> ResponseEntity.ok(UserSubscriptionResourceFromEntityAssembler.toResourceFromEntity(s)))
            .orElseGet(() -> ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.notFound("UserSubscription", String.valueOf(id))));
    }
    @PostMapping(consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Subscribe the authenticated rental company to an active plan")
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserSubscriptionResource resource,
                                     Authentication authentication) {
        if (!ownership.canManage(authentication,resource.userId())) return forbidden();
        if (resource.id()!=null && resource.id()!=0 || resource.status()!=SubscriptionStatus.ACTIVE)
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.validationError("subscription", "id must be 0/omitted and status ACTIVE"));
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(new CreateUserSubscriptionCommand(resource.userId(), resource.planId(),
                resource.startDate(), resource.endDate(), resource.autoRenew())),
            UserSubscriptionResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.CREATED);
    }
    /** Angular sends the existing subscription representation; only planId is mutable here. */
    @PutMapping(value="/{id}",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Change the authenticated rental company's active subscription plan")
    public ResponseEntity<?> changePlan(@PathVariable Long id, @RequestBody UserSubscriptionResource resource,
                                         Authentication authentication) {
        var actor = ownership.authenticatedOwnerId(authentication);
        if (actor.isEmpty()) return forbidden();
        if (resource == null || resource.planId() == null || resource.planId() <= 0)
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.validationError("planId", "A positive planId is required"));
        if (resource.id() != null && !id.equals(resource.id()))
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.validationError("subscriptionId", "Request id must match the path id"));
        if (resource.userId() != null && !actor.get().equals(resource.userId()))
            return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(new ChangeUserSubscriptionPlanCommand(id,resource.planId(),actor.get())),
            UserSubscriptionResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary="Cancel a subscription belonging to the authenticated rental company")
    public ResponseEntity<?> cancel(@PathVariable Long id, Authentication authentication) {
        var actor = ownership.authenticatedOwnerId(authentication);
        if (actor.isEmpty()) return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(new CancelUserSubscriptionCommand(id,actor.get())),
            UserSubscriptionResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
    }
    private static ResponseEntity<ProblemDetail> forbidden() {
        var problem=ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
            "Only the authenticated rental company may manage its own subscription");
        problem.setTitle("Forbidden");problem.setProperty("code","SUBSCRIPTION_FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }
}

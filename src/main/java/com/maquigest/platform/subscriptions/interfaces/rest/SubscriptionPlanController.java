package com.maquigest.platform.subscriptions.interfaces.rest;
import com.maquigest.platform.subscriptions.application.queryservices.SubscriptionPlanQueryService;
import com.maquigest.platform.subscriptions.domain.model.queries.GetAllSubscriptionPlansQuery;
import com.maquigest.platform.subscriptions.interfaces.rest.transform.SubscriptionPlanResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@RestController
@RequestMapping(value="/api/v1/subscription-plans", produces=APPLICATION_JSON_VALUE)
@Tag(name="Subscription Plans",description="Subscription plan catalog")
@SecurityRequirement(name="bearerAuth")
public class SubscriptionPlanController {
    private final SubscriptionPlanQueryService queries;
    public SubscriptionPlanController(SubscriptionPlanQueryService queries) { this.queries=queries; }
    @GetMapping @Operation(summary="List subscription plans")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(queries.handle(new GetAllSubscriptionPlansQuery()).stream()
            .map(SubscriptionPlanResourceFromEntityAssembler::toResourceFromEntity).toList());
    }
}

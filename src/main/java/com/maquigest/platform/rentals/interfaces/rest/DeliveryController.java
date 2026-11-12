package com.maquigest.platform.rentals.interfaces.rest;
import com.maquigest.platform.rentals.application.commandservices.RentalLifecycleCommandService;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.commands.RegisterDeliveryCommand;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.rentals.interfaces.rest.resources.DeliveryResource;
import com.maquigest.platform.rentals.interfaces.rest.transform.RentalResourceAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@RestController
@RequestMapping(value="/api/v1/deliveries",produces=APPLICATION_JSON_VALUE)
@Tag(name="Deliveries",description="Register equipment handover")
@SecurityRequirement(name="bearerAuth")
public class DeliveryController {
    private final RentalLifecycleCommandService commands;private final RentalQueryService queries;private final RentalActorPolicy policy;
    public DeliveryController(RentalLifecycleCommandService commands,RentalQueryService queries,RentalActorPolicy policy){
        this.commands=commands;this.queries=queries;this.policy=policy;
    }
    private Long actor(Authentication auth,boolean management) {
        var a=policy.current(auth).orElseThrow(RentalsException::forbidden);
        if(management&&!a.rentalCompany()) throw RentalsException.forbidden();
        return a.userId();
    }
    @GetMapping @Operation(summary="List deliveries for visible rentals")
    public List<DeliveryResource> all(Authentication auth){return queries.deliveriesFor(actor(auth,false)).stream()
        .map(RentalResourceAssembler::from).toList();}
    @PostMapping(consumes=APPLICATION_JSON_VALUE) @Operation(summary="Register delivery atomically and activate rental")
    public ResponseEntity<DeliveryResource> create(@RequestBody DeliveryResource body,Authentication auth) {
        if(body==null) throw RentalsException.invalid("Delivery body is required");
        var delivered=ResponseEntityAssembler.valueOrThrow(commands.handle(new RegisterDeliveryCommand(body.rentalId(),actor(auth,true),body.deliveredAt(),body.notes())), RentalsException::fromApplicationError);
        return ResponseEntity.status(HttpStatus.CREATED).body(RentalResourceAssembler.from(delivered));
    }
}

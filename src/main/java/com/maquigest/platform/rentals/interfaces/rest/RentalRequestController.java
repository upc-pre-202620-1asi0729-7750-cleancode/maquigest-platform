package com.maquigest.platform.rentals.interfaces.rest;

import com.maquigest.platform.rentals.application.commandservices.RentalRequestCommandService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.queries.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import com.maquigest.platform.rentals.interfaces.rest.security.RentalActorPolicy;
import com.maquigest.platform.rentals.interfaces.rest.resources.RentalRequestResource;
import com.maquigest.platform.rentals.interfaces.rest.resources.RescheduleRentalRequestResource;
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
@RequestMapping(value="/api/v1/rental-requests", produces=APPLICATION_JSON_VALUE)
@Tag(name="Rental Requests", description="Construction requests and owner decision workflow")
@SecurityRequirement(name="bearerAuth")
public class RentalRequestController {
    private final RentalRequestCommandService commands;
    private final RentalQueryService queries;
    private final RentalActorPolicy policy;

    public RentalRequestController(RentalRequestCommandService commands, RentalQueryService queries,
                                   RentalActorPolicy policy) {
        this.commands = commands;
        this.queries = queries;
        this.policy = policy;
    }

    private RentalActorPolicy.Actor current(Authentication auth) {
        return policy.current(auth).orElseThrow(RentalsException::forbidden);
    }

    @GetMapping
    @Operation(summary="List only requests involving the signed-in user")
    public List<RentalRequestResource> all(Authentication auth) {
        var actor = current(auth);
        return queries.handle(new GetRentalRequestsQuery(actor.userId())).stream()
            .map(RentalResourceAssembler::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary="Read one rental request if participant")
    public RentalRequestResource one(@PathVariable Long id, Authentication auth) {
        var actor = current(auth);
        return queries.handle(new GetRentalRequestByIdQuery(id, actor.userId()))
            .map(RentalResourceAssembler::from)
            .orElseThrow(() -> RentalsException.notFound("RentalRequest"));
    }

    @PostMapping(consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Submit a pending request (construction company)")
    public ResponseEntity<RentalRequestResource> create(@RequestBody RentalRequestResource body, Authentication auth) {
        var actor = current(auth);
        if (!actor.constructionCompany() || body == null || !actor.userId().equals(body.constructionUserId()))
            throw RentalsException.forbidden();
        if (body.status() != null && body.status() != RentalRequestStatus.PENDING)
            throw RentalsException.invalid("New requests must be PENDING");
        var created = ResponseEntityAssembler.valueOrThrow(commands.handle(new CreateRentalRequestCommand(body.equipmentId(), actor.userId(),
            body.rentalCompanyUserId(), RentalResourceAssembler.parsePeriod(body.startDate(), body.endDate()))), RentalsException::fromApplicationError);
        return ResponseEntity.status(HttpStatus.CREATED).body(RentalResourceAssembler.from(created));
    }

    @PatchMapping(value="/{id}/period", consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Change dates of your own PENDING rental request")
    public RentalRequestResource reschedule(@PathVariable Long id,
                                             @RequestBody RescheduleRentalRequestResource body,
                                             Authentication auth) {
        var actor = current(auth);
        if (!actor.constructionCompany()) throw RentalsException.forbidden();
        if (body == null) throw RentalsException.invalid("New rental dates are required");
        var newPeriod = RentalResourceAssembler.parsePeriod(body.startDate(), body.endDate());
        return RentalResourceAssembler.from(
            ResponseEntityAssembler.valueOrThrow(commands.handle(new RescheduleRentalRequestCommand(id, actor.userId(), newPeriod)), RentalsException::fromApplicationError));
    }

    @PutMapping(value="/{id}", consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Approve or reject (authenticated equipment owner)")
    public RentalRequestResource update(@PathVariable Long id, @RequestBody RentalRequestResource body, Authentication auth) {
        var actor = current(auth);
        if (!actor.rentalCompany()) throw RentalsException.forbidden();
        if (body == null || body.status() == null) throw RentalsException.invalid("Request status is required");
        var original = queries.handle(new GetRentalRequestByIdQuery(id, actor.userId()))
            .orElseThrow(() -> RentalsException.notFound("RentalRequest"));
        if (!original.getRentalCompanyUserId().equals(actor.userId())) throw RentalsException.forbidden();
        if (!Objects.equals(body.id(), id) || !Objects.equals(body.equipmentId(), original.getEquipmentId())
            || !Objects.equals(body.constructionUserId(), original.getConstructionUserId())
            || !Objects.equals(body.rentalCompanyUserId(), original.getRentalCompanyUserId())
            || !RentalResourceAssembler.parsePeriod(body.startDate(), body.endDate()).equals(original.getPeriod()))
            throw RentalsException.invalid("Request participants, equipment and dates cannot be edited");
        return RentalResourceAssembler.from(ResponseEntityAssembler.valueOrThrow(commands.handle(new ResolveRentalRequestCommand(id, actor.userId(), body.status())), RentalsException::fromApplicationError));
    }
}

package com.maquigest.platform.rentals.interfaces.rest;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.queries.*;
import com.maquigest.platform.rentals.infrastructure.acl.iam.RentalActorPolicy;
import com.maquigest.platform.rentals.interfaces.rest.resources.RentalResource;
import com.maquigest.platform.rentals.interfaces.rest.transform.RentalResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@RestController
@RequestMapping(value="/api/v1/rentals",produces=APPLICATION_JSON_VALUE)
@Tag(name="Rentals",description="Rentals created exclusively by approved rental requests")
@SecurityRequirement(name="bearerAuth")
public class RentalController {
    private final RentalQueryService queries; private final RentalActorPolicy policy;
    public RentalController(RentalQueryService queries,RentalActorPolicy policy){this.queries=queries;this.policy=policy;}
    private Long actor(Authentication auth){return policy.current(auth).orElseThrow(RentalsException::forbidden).userId();}
    @GetMapping @Operation(summary="List rentals visible to authenticated participant")
    public List<RentalResource> all(Authentication auth) {
        return queries.handle(new GetRentalsQuery(actor(auth))).stream().map(RentalResourceAssembler::from).toList();
    }
    @GetMapping("/{id}") @Operation(summary="Get one rental if participant")
    public RentalResource one(@PathVariable Long id,Authentication auth) {
        return queries.handle(new GetRentalByIdQuery(id,actor(auth)))
            .map(RentalResourceAssembler::from).orElseThrow(()->RentalsException.notFound("Rental"));
    }
    // Retained for Angular endpoint compatibility. No client may create or set rental status directly.
    @PostMapping(consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Reserved for server-approved rentals; direct creation forbidden")
    public ResponseEntity<?> directCreate(@RequestBody RentalResource ignored,Authentication auth) {
        actor(auth);
        throw RentalsException.conflict("Approve a rental request instead; the server creates its rental and reservation atomically");
    }
    @PutMapping(value="/{id}",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Read-only compatibility: direct status changes are not permitted")
    public RentalResource directUpdate(@PathVariable Long id,@RequestBody RentalResource body,Authentication auth) {
        var existing=one(id,auth);
        if(body!=null && Objects.equals(body.id(),id) && existing.equals(body)) return existing;
        throw RentalsException.conflict("Use POST /deliveries or POST /equipment-returns for rental lifecycle transitions");
    }
}

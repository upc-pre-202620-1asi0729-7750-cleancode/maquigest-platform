package com.maquigest.platform.rentals.interfaces.rest;
import com.maquigest.platform.rentals.application.commandservices.RentalLifecycleCommandService;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.commands.RegisterReturnCommand;
import com.maquigest.platform.rentals.infrastructure.acl.iam.RentalActorPolicy;
import com.maquigest.platform.rentals.interfaces.rest.resources.EquipmentReturnResource;
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
@RequestMapping(value="/api/v1/equipment-returns",produces=APPLICATION_JSON_VALUE)
@Tag(name="Equipment Returns",description="Complete rentals and release only their own reservation")
@SecurityRequirement(name="bearerAuth")
public class EquipmentReturnController {
    private final RentalLifecycleCommandService commands;private final RentalQueryService queries;private final RentalActorPolicy policy;
    public EquipmentReturnController(RentalLifecycleCommandService commands,RentalQueryService queries,RentalActorPolicy policy){
        this.commands=commands;this.queries=queries;this.policy=policy;
    }
    private Long actor(Authentication auth,boolean management) {
        var a=policy.current(auth).orElseThrow(RentalsException::forbidden);
        if(management&&!a.rentalCompany()) throw RentalsException.forbidden();
        return a.userId();
    }
    @GetMapping @Operation(summary="List equipment returns for visible rentals")
    public List<EquipmentReturnResource> all(Authentication auth){return queries.returnsFor(actor(auth,false)).stream()
        .map(RentalResourceAssembler::from).toList();}
    @PostMapping(consumes=APPLICATION_JSON_VALUE) @Operation(summary="Register return, complete rental and release Inventory reservation atomically")
    public ResponseEntity<EquipmentReturnResource> create(@RequestBody EquipmentReturnResource body,Authentication auth) {
        if(body==null) throw RentalsException.invalid("Equipment return body is required");
        var returned=commands.handle(new RegisterReturnCommand(body.rentalId(),actor(auth,true),body.returnedAt(),body.notes(),body.maintenanceRequired()));
        return ResponseEntity.status(HttpStatus.CREATED).body(RentalResourceAssembler.from(returned));
    }
}

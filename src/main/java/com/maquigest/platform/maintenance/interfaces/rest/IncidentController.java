package com.maquigest.platform.maintenance.interfaces.rest;
import com.maquigest.platform.maintenance.application.commandservices.IncidentCommandService;
import com.maquigest.platform.maintenance.application.queryservices.MaintenanceQueryService;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.queries.*;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import com.maquigest.platform.maintenance.interfaces.rest.security.MaintenanceActorPolicy;
import com.maquigest.platform.maintenance.interfaces.rest.resources.*;
import com.maquigest.platform.maintenance.interfaces.rest.transform.MaintenanceResourceAssembler;
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
@RequestMapping(value="/api/v1/incidents",produces=APPLICATION_JSON_VALUE)
@Tag(name="Incidents",description="Equipment incident reporting, resolution and rental restrictions")
@SecurityRequirement(name="bearerAuth")
public class IncidentController {
    private final IncidentCommandService commands;private final MaintenanceQueryService queries;
    private final MaintenanceActorPolicy policy;
    public IncidentController(IncidentCommandService commands,MaintenanceQueryService queries,MaintenanceActorPolicy policy){
        this.commands=commands;this.queries=queries;this.policy=policy;
    }
    @GetMapping @Operation(summary="List company incidents or inspect a machine's public rental restrictions")
    public List<IncidentResource> list(@RequestParam(required=false) Long equipmentId,Authentication auth){
        var actor=policy.current(auth);
        if(!actor.rentalCompany()&&equipmentId==null)throw MaintenanceException.forbidden();
        var results=queries.handle(new GetIncidentsQuery(actor.userId(),equipmentId,actor.rentalCompany()));
        return results.stream().map(i->actor.rentalCompany()?MaintenanceResourceAssembler.from(i):
           MaintenanceResourceAssembler.publicBlocker(i)).toList();
    }
    @GetMapping("/{id}") @Operation(summary="Read an owned incident")
    public IncidentResource one(@PathVariable Long id,Authentication auth){var actor=policy.requireOwner(auth);
        return queries.handle(new GetIncidentByIdQuery(id,actor.userId())).map(MaintenanceResourceAssembler::from)
            .orElseThrow(()->MaintenanceException.notFound("Incident"));}
    @PostMapping(consumes=APPLICATION_JSON_VALUE) @Operation(summary="Report incident; blocking incidents atomically mark Inventory as MAINTENANCE")
    public ResponseEntity<IncidentResource> create(@RequestBody IncidentResource input,Authentication auth){
        var actor=policy.requireOwner(auth);
        if(input==null||input.id()!=null&&input.id()!=0)throw MaintenanceException.invalid("New incident must not have an id");
        if(input.status()!=null&&input.status()!=IncidentStatus.OPEN)throw MaintenanceException.invalid("New incident status must be OPEN");
        if(input.resolvedAt()!=null)throw MaintenanceException.invalid("New incident cannot be resolved");
        var created=ResponseEntityAssembler.valueOrThrow(commands.handle(new ReportIncidentCommand(input.equipmentId(),input.description(),Boolean.TRUE.equals(input.blocksRental())),actor.userId()), MaintenanceException::fromApplicationError);
        return ResponseEntity.status(HttpStatus.CREATED).body(MaintenanceResourceAssembler.from(created));
    }
    @PatchMapping(value="/{id}",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Resolve an incident or make an open incident block rentals")
    public IncidentResource patch(@PathVariable Long id,@RequestBody IncidentPatchResource body,Authentication auth){
        var actor=policy.requireOwner(auth);
        if(body==null)throw MaintenanceException.invalid("Patch body is required");
        var cmd=new PatchIncidentCommand(id,body.blocksRental(),body.status(),
            MaintenanceResourceAssembler.parseOptional(body.resolvedAt(),"resolvedAt"));
        return MaintenanceResourceAssembler.from(ResponseEntityAssembler.valueOrThrow(commands.handle(cmd,actor.userId()), MaintenanceException::fromApplicationError));
    }
    @PostMapping(value="/equipment/{equipmentId}/reactivate",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Reactivate after inspection; rejects unresolved blockers and active rentals")
    public ResponseEntity<Void> reactivate(@PathVariable Long equipmentId,@RequestBody InspectionResource body,Authentication auth){
        var actor=policy.requireOwner(auth);
        ResponseEntityAssembler.valueOrThrow(commands.handle(new ReactivateEquipmentCommand(equipmentId,body!=null&&Boolean.TRUE.equals(body.inspectionConfirmed())),actor.userId()), MaintenanceException::fromApplicationError);
        return ResponseEntity.noContent().build();
    }
}

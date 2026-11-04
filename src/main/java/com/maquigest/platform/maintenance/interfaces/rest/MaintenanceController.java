package com.maquigest.platform.maintenance.interfaces.rest;
import com.maquigest.platform.maintenance.application.commandservices.MaintenanceCommandService;
import com.maquigest.platform.maintenance.application.queryservices.MaintenanceQueryService;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.queries.*;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.infrastructure.acl.iam.MaintenanceActorPolicy;
import com.maquigest.platform.maintenance.interfaces.rest.resources.*;
import com.maquigest.platform.maintenance.interfaces.rest.transform.MaintenanceResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@RestController
@RequestMapping(value="/api/v1/maintenances",produces=APPLICATION_JSON_VALUE)
@Tag(name="Maintenances",description="Maintenance scheduling, execution, tracking and history")
@SecurityRequirement(name="bearerAuth")
public class MaintenanceController {
    private final MaintenanceCommandService commands;private final MaintenanceQueryService queries;
    private final MaintenanceActorPolicy policy;
    public MaintenanceController(MaintenanceCommandService commands,MaintenanceQueryService queries,MaintenanceActorPolicy policy){
        this.commands=commands;this.queries=queries;this.policy=policy;
    }
    @GetMapping @Operation(summary="List maintenances owned by the authenticated rental company")
    public List<MaintenanceResource> list(@RequestParam(required=false) Long equipmentId,Authentication auth){
        var actor=policy.requireOwner(auth);
        return queries.handle(new GetMaintenancesQuery(actor.userId(),equipmentId)).stream().map(MaintenanceResourceAssembler::from).toList();
    }
    @GetMapping("/{id}")
    public MaintenanceResource one(@PathVariable Long id,Authentication auth){var actor=policy.requireOwner(auth);
        return queries.handle(new GetMaintenanceByIdQuery(id,actor.userId())).map(MaintenanceResourceAssembler::from)
            .orElseThrow(()->MaintenanceException.notFound("Maintenance"));}
    @PostMapping(consumes=APPLICATION_JSON_VALUE) @Operation(summary="Register completed maintenance or schedule a new one (Growth plan)")
    public ResponseEntity<MaintenanceResource> create(@RequestBody MaintenanceResource input,Authentication auth){
        var actor=policy.requireOwner(auth);
        if(input==null||(input.id()!=null&&input.id()!=0))throw MaintenanceException.invalid("New maintenance must not have an id");
        if(input.status()!=MaintenanceStatus.COMPLETED&&input.status()!=MaintenanceStatus.SCHEDULED)
            throw MaintenanceException.invalid("New maintenance must be COMPLETED or SCHEDULED");
        var cmd=new RegisterMaintenanceCommand(input.equipmentId(),
            MaintenanceResourceAssembler.parse(input.performedAt(),"performedAt"),input.type(),input.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(MaintenanceResourceAssembler.from(commands.handle(cmd,actor.userId())));
    }
    @PatchMapping(value="/{id}",consumes=APPLICATION_JSON_VALUE) @Operation(summary="Update lifecycle status, assign technician, acknowledge work and record progress")
    public MaintenanceResource patch(@PathVariable Long id,@RequestBody MaintenancePatchResource input,Authentication auth){
        var actor=policy.requireOwner(auth);
        if(input==null)throw MaintenanceException.invalid("Patch body is required");
        var cmd=new AdvanceMaintenanceCommand(id,input.status(),input.technician(),Boolean.TRUE.equals(input.acknowledge()),input.notes());
        return MaintenanceResourceAssembler.from(commands.handle(cmd,actor.userId()));
    }
    @GetMapping("/{id}/activities") @Operation(summary="Read maintenance tracking history")
    public List<MaintenanceActivityResource> activities(@PathVariable Long id,Authentication auth){var actor=policy.requireOwner(auth);
        return queries.activities(id,actor.userId()).stream().map(MaintenanceResourceAssembler::from).toList();}
    @GetMapping("/equipment/{equipmentId}/history") @Operation(summary="Read merged rental, incident and maintenance timeline (owner only)")
    public EquipmentHistoryResource history(@PathVariable Long equipmentId,Authentication auth){var actor=policy.requireOwner(auth);
        return MaintenanceResourceAssembler.from(queries.handle(new GetEquipmentHistoryQuery(equipmentId,actor.userId())));}
}

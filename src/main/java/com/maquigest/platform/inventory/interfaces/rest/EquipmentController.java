package com.maquigest.platform.inventory.interfaces.rest;

import com.maquigest.platform.inventory.application.commandservices.EquipmentCommandService;
import com.maquigest.platform.inventory.application.queryservices.EquipmentQueryService;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.DeleteEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.commands.ReserveEquipmentPeriodCommand;
import com.maquigest.platform.inventory.domain.model.commands.ChangeEquipmentOperationalStatusCommand;
import com.maquigest.platform.inventory.domain.model.queries.*;
import com.maquigest.platform.inventory.infrastructure.acl.iam.InventoryOwnershipPolicy;
import com.maquigest.platform.inventory.interfaces.rest.resources.*;
import com.maquigest.platform.inventory.interfaces.rest.transform.*;
import com.maquigest.platform.shared.application.result.*;
import com.maquigest.platform.shared.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** REST contract for WebApp Inventory. Ownership is checked against IAM, never the request body alone. */
@RestController
@RequestMapping(value="/api/v1/equipment",produces=APPLICATION_JSON_VALUE)
@Tag(name="Equipment",description="Equipment catalogue, ownership and availability")
@SecurityRequirement(name="bearerAuth")
public class EquipmentController {
    private final EquipmentCommandService commands;
    private final EquipmentQueryService queries;
    private final InventoryOwnershipPolicy ownership;
    public EquipmentController(EquipmentCommandService commands, EquipmentQueryService queries,
        InventoryOwnershipPolicy ownership) {
        this.commands=commands; this.queries=queries; this.ownership=ownership;
    }

    @GetMapping
    @Operation(summary="Get equipment in the marketplace or filter by owner")
    public ResponseEntity<?> getAll(@RequestParam(required=false) Long userId) {
        if (userId!=null && userId<=0)
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.validationError("userId","userId must be positive"));
        List<EquipmentResource> resources = queries.handle(new GetAllEquipmentQuery(userId)).stream()
            .map(EquipmentResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary="Get equipment by id")
    @ApiResponse(responseCode="404", description="Equipment does not exist")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Result<Equipment,ApplicationError> result = queries.handle(new GetEquipmentByIdQuery(id))
            .<Result<Equipment,ApplicationError>>map(Result::success)
            .orElseGet(() -> Result.failure(ApplicationError.notFound("Equipment",String.valueOf(id))));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
            EquipmentResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
    }

    @PostMapping(consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Register equipment (US06)")
    @ApiResponse(responseCode="201",description="Equipment registered")
    public ResponseEntity<?> create(@Valid @RequestBody CreateEquipmentResource resource,Authentication authentication) {
        if (!ownership.canCreate(authentication, resource.userId())) return forbidden();
        var command = CreateEquipmentCommandFromResourceAssembler.toCommandFromResource(resource);
        Result<Equipment,ApplicationError> result = commands.handle(command)
            .flatMap(id -> queries.handle(new GetEquipmentByIdQuery(id))
                .<Result<Equipment,ApplicationError>>map(Result::success)
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Equipment",String.valueOf(id)))));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
            EquipmentResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.CREATED);
    }

    @PatchMapping(value="/{id}",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Update only editable equipment information (US08)")
    @ApiResponse(responseCode="403",description="Caller is not the owner")
    public ResponseEntity<?> update(@PathVariable Long id,@Valid @RequestBody UpdateEquipmentResource resource,
        Authentication authentication) {
        if (!ownership.canManage(authentication,id)) return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(UpdateEquipmentCommandFromResourceAssembler.toCommandFromResource(id,resource)),
            EquipmentResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
    }

    @PostMapping(value="/{id}/availability-blocks",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Reserve an inclusive equipment calendar period atomically")
    @ApiResponse(responseCode="409",description="Equipment unavailable or date range overlaps")
    public ResponseEntity<?> reservePeriod(@PathVariable Long id,
        @Valid @RequestBody ReserveEquipmentPeriodResource resource, Authentication authentication) {
        if (!ownership.canManage(authentication,id)) return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(new ReserveEquipmentPeriodCommand(id,resource.startDate(),resource.endDate())),
            EquipmentResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.CREATED);
    }

    @PatchMapping(value="/{id}/operational-status",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Change equipment operational status using aggregate transition rules")
    @ApiResponse(responseCode="409",description="Operational transition not allowed")
    public ResponseEntity<?> changeStatus(@PathVariable Long id,
        @Valid @RequestBody ChangeEquipmentOperationalStatusResource resource, Authentication authentication) {
        if (!ownership.canManage(authentication,id)) return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(new ChangeEquipmentOperationalStatusCommand(id,resource.status())),
            EquipmentResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary="Delete owned equipment only when not in use")
    @ApiResponse(responseCode="204",description="Equipment deleted")
    public ResponseEntity<?> delete(@PathVariable Long id,Authentication authentication) {
        if (!ownership.canManage(authentication,id)) return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(commands.handle(new DeleteEquipmentCommand(id)),
            ignored -> null,HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary="Check availability over an inclusive date range (US13)")
    public ResponseEntity<?> checkAvailability(@PathVariable Long id,
        @RequestParam String startDate,@RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            var query = new CheckEquipmentAvailabilityQuery(id,start,end);
            Result<EquipmentAvailabilityResource,ApplicationError> result = queries.handle(query)
                .<Result<EquipmentAvailabilityResource,ApplicationError>>map(available ->
                    Result.success(new EquipmentAvailabilityResource(id,start,end,available)))
                .orElseGet(() -> Result.failure(ApplicationError.notFound("Equipment",String.valueOf(id))));
            return ResponseEntityAssembler.toResponseEntityFromResult(result,r->r,HttpStatus.OK);
        } catch (DateTimeParseException | IllegalArgumentException ex) {
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.validationError("period",ex.getMessage()));
        }
    }

    private static ResponseEntity<ProblemDetail> forbidden() {
        var problem=ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
            "Only the authenticated rental company that owns the equipment may modify it");
        problem.setTitle("Forbidden");
        problem.setProperty("code","EQUIPMENT_FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }
}

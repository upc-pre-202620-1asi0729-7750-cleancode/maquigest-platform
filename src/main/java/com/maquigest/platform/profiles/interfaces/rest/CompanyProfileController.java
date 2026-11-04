package com.maquigest.platform.profiles.interfaces.rest;
import com.maquigest.platform.profiles.application.commandservices.CompanyProfileCommandService;
import com.maquigest.platform.profiles.application.queryservices.CompanyProfileQueryService;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.queries.*;
import com.maquigest.platform.profiles.infrastructure.acl.iam.ProfileOwnershipPolicy;
import com.maquigest.platform.profiles.interfaces.rest.resources.*;
import com.maquigest.platform.profiles.interfaces.rest.transform.*;
import com.maquigest.platform.shared.application.result.*;
import com.maquigest.platform.shared.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Compatible with Angular ProfilesApiEndpoint and Rentals Profiles ACL. */
@RestController
@RequestMapping(value="/api/v1/profiles",produces=APPLICATION_JSON_VALUE)
@Tag(name="Profiles",description="Company profile management")
@SecurityRequirement(name="bearerAuth")
public class CompanyProfileController {
    private final CompanyProfileCommandService commands;
    private final CompanyProfileQueryService queries;
    private final ProfileOwnershipPolicy policy;
    public CompanyProfileController(CompanyProfileCommandService commands,CompanyProfileQueryService queries,
                                    ProfileOwnershipPolicy policy){
        this.commands=commands;this.queries=queries;this.policy=policy;
    }
    @GetMapping
    @Operation(summary="List business profiles or filter by IAM userId")
    public ResponseEntity<?> all(@RequestParam(required=false) Long userId){
        if(userId!=null && userId<=0)return ProblemDetailFromApplicationErrorAssembler
            .toResponseFromApplicationError(ApplicationError.validationError("userId","Must be positive"));
        List<CompanyProfileResource> list=queries.handle(new GetAllCompanyProfilesQuery(userId)).stream()
            .map(CompanyProfileResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    @Operation(summary="Find a company profile by profile id")
    public ResponseEntity<?> byId(@PathVariable Long id){
        var result=queries.handle(new GetCompanyProfileByIdQuery(id));
        if(result.isEmpty())return ProblemDetailFromApplicationErrorAssembler
            .toResponseFromApplicationError(ApplicationError.notFound("Profile",String.valueOf(id)));
        return ResponseEntity.ok(CompanyProfileResourceFromEntityAssembler.toResourceFromEntity(result.get()));
    }
    @PostMapping(consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Create profile for the authenticated IAM company")
    public ResponseEntity<?> create(@Valid @RequestBody CreateCompanyProfileResource resource,
                                    Authentication authentication){
        if(!policy.canCreate(authentication,resource.userId()))return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(CreateCompanyProfileCommandFromResourceAssembler.toCommandFromResource(resource)),
            CompanyProfileResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.CREATED);
    }
    @PutMapping(value="/{id}",consumes=APPLICATION_JSON_VALUE)
    @Operation(summary="Update an owned company profile without changing IAM userId")
    public ResponseEntity<?> update(@PathVariable Long id,@Valid @RequestBody UpdateCompanyProfileResource resource,
                                    Authentication authentication){
        if(resource.id()!=null && !resource.id().equals(id))return ProblemDetailFromApplicationErrorAssembler
            .toResponseFromApplicationError(ApplicationError.validationError("id","Body id must match URL"));
        if(!policy.canModify(authentication,id))return forbidden();
        return ResponseEntityAssembler.toResponseEntityFromResult(
            commands.handle(UpdateCompanyProfileCommandFromResourceAssembler.toCommandFromResource(id,resource)),
            CompanyProfileResourceFromEntityAssembler::toResourceFromEntity,HttpStatus.OK);
    }
    private static ResponseEntity<ProblemDetail> forbidden(){
        var body=ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
            "Only the authenticated company may create or update its own profile");
        body.setTitle("Forbidden");body.setProperty("code","PROFILE_FORBIDDEN");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}

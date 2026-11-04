package com.maquigest.platform.iam.interfaces.rest;

import com.maquigest.platform.iam.application.commandservices.UserCommandService;
import com.maquigest.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.maquigest.platform.iam.interfaces.rest.resources.SignInResource;
import com.maquigest.platform.iam.interfaces.rest.resources.SignUpResource;
import com.maquigest.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.maquigest.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.maquigest.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.maquigest.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** IAM sign-up and sign-in REST controller, adapted to the MaquiGest Angular API contract. */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "MaquiGest account registration and authentication")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping(value = "/sign-up", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a rental or construction company account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Invalid registration data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpResource resource) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PostMapping(value = "/sign-in", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sign in and issue JWT", description = "Authenticate with email and password to receive a signed bearer token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthenticatedUserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email or password")
    })
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInResource resource) {
        var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result,
                auth -> AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                        auth.getLeft(), auth.getRight()),
                HttpStatus.OK);
    }
}

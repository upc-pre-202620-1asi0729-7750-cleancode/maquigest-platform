package com.maquigest.platform.iam.interfaces.rest;

import com.maquigest.platform.iam.application.commandservices.UserCommandService;
import com.maquigest.platform.iam.interfaces.rest.resources.SignUpResource;
import com.maquigest.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.maquigest.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Authentication resource endpoint. Sign-in and JWT are added in the next IAM block. */
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
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpResource resource) {
        var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = userCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, UserResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }
}

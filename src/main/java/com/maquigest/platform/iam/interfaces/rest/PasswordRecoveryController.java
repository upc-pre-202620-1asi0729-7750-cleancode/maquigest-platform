package com.maquigest.platform.iam.interfaces.rest;
import com.maquigest.platform.iam.application.commandservices.PasswordRecoveryCommandService;
import com.maquigest.platform.iam.interfaces.rest.resources.*;
import com.maquigest.platform.iam.interfaces.rest.transform.PasswordRecoveryCommandFromResourceAssembler;
import com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import com.maquigest.platform.shared.application.result.Result;
import com.maquigest.platform.shared.application.result.ApplicationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication")
public class PasswordRecoveryController {
    private final PasswordRecoveryCommandService commands;
    public PasswordRecoveryController(PasswordRecoveryCommandService commands) { this.commands = commands; }
    @PostMapping(value = "/password-recovery", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Request password recovery without disclosing account existence")
    @ApiResponse(responseCode = "202", description = "Request accepted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<?> request(@Valid @RequestBody PasswordRecoveryResource resource) {
        return response(commands.handle(PasswordRecoveryCommandFromResourceAssembler.toCommandFromResource(resource)), HttpStatus.ACCEPTED);
    }
    @PostMapping(value = "/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Consume a password recovery token and replace the password")
    @ApiResponse(responseCode = "204", description = "Password reset")
    @ApiResponse(responseCode = "400", description = "Invalid or expired recovery link")
    public ResponseEntity<?> reset(@Valid @RequestBody ResetPasswordResource resource) {
        return response(commands.handle(PasswordRecoveryCommandFromResourceAssembler.toCommandFromResource(resource)), HttpStatus.NO_CONTENT);
    }
    private static ResponseEntity<?> response(Result<Boolean, ApplicationError> result, HttpStatus status) {
        if (result instanceof Result.Failure<Boolean, ApplicationError> failure)
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).build();
    }
}

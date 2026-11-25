package com.maquigest.platform.iam.interfaces.rest;
import com.maquigest.platform.iam.application.commandservices.EmailVerificationCommandService;
import com.maquigest.platform.iam.interfaces.rest.resources.*;
import com.maquigest.platform.iam.interfaces.rest.transform.EmailVerificationCommandFromResourceAssembler;
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
public class EmailVerificationController {
    private final EmailVerificationCommandService commands;
    public EmailVerificationController(EmailVerificationCommandService commands) { this.commands = commands; }
    @PostMapping(value = "/resend-email-verification", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Resend verification without disclosing account existence")
    @ApiResponse(responseCode = "202", description = "Request accepted")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    public ResponseEntity<?> request(@Valid @RequestBody ResendEmailVerificationResource resource) {
        return response(commands.handle(EmailVerificationCommandFromResourceAssembler.toCommandFromResource(resource)), HttpStatus.ACCEPTED);
    }
    @PostMapping(value = "/verify-email", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Confirm email ownership and registration password")
    @ApiResponse(responseCode = "204", description = "Email verified")
    @ApiResponse(responseCode = "400", description = "Invalid verification link or password")
    public ResponseEntity<?> reset(@Valid @RequestBody VerifyEmailResource resource) {
        return response(commands.handle(EmailVerificationCommandFromResourceAssembler.toCommandFromResource(resource)), HttpStatus.NO_CONTENT);
    }
    private static ResponseEntity<?> response(Result<Boolean, ApplicationError> result, HttpStatus status) {
        if (result instanceof Result.Failure<Boolean, ApplicationError> failure)
            return ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).build();
    }
}

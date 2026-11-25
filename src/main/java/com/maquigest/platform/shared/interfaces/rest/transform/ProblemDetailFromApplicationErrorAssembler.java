package com.maquigest.platform.shared.interfaces.rest.transform;

import com.maquigest.platform.shared.application.result.ApplicationError;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Assembler for transforming {@link ApplicationError} instances into RFC 9457 {@link ProblemDetail} responses.
 */
@NullMarked
public final class ProblemDetailFromApplicationErrorAssembler {

    private static final String MESSAGES_BASENAME = "messages";
    private static final String ERROR_TYPE_PREFIX = "/api/v1/errors/";

    private ProblemDetailFromApplicationErrorAssembler() {
    }

    /**
     * Transforms an {@link ApplicationError} into a Spring {@link ProblemDetail}.
     *
     * @param error the application error to transform
     * @return the assembled ProblemDetail
     */
    public static ProblemDetail toProblemDetailFromApplicationError(ApplicationError error) {
        HttpStatusCode status = toStatusFromErrorCode(error.code());
        // Never pass technical server-error details to a message template or response.
        boolean unexpected = status.value() == 500;
        String localizedMessage = unexpected
                ? toLocalizedMessageWithFallback("error.unexpected.message", "Unexpected error")
                : toLocalizedMessageFromApplicationError(error);

        ProblemDetail problemDetail = createProblem(status, error.code(), localizedMessage, toProblemTitle(error.code(), status));
        problemDetail.setType(toProblemTypeUri(error.code()));

        if (unexpected) {
            problemDetail.setProperty("details", "An unexpected error occurred");
        } else if (error.details() != null && !error.details().isBlank()) {
            problemDetail.setProperty("details", error.details());
        }

        return problemDetail;
    }

    /**
     * Transforms an {@link ApplicationError} into a {@link ResponseEntity} containing a {@link ProblemDetail}.
     *
     * @param error the application error to transform
     * @return response entity with corresponding HTTP status and ProblemDetail body
     */
    public static ResponseEntity<ProblemDetail> toResponseFromApplicationError(ApplicationError error) {
        ProblemDetail problemDetail = toProblemDetailFromApplicationError(error);
        return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
    }

    /**
     * Preserve the published about:blank contract of context-specific errors.
     * The boundary resolves the existing localized detail and title before assembly.
     */
    public static ProblemDetail toProblemDetailFromApplicationError(ApplicationError error, String title) {
        HttpStatusCode status = toStatusFromErrorCode(error.code());
        if (status.value() == 500) {
            return toProblemDetailFromApplicationError(error);
        }
        return createProblem(status, error.code(), error.message(), title);
    }

    public static ResponseEntity<ProblemDetail> toResponseFromApplicationError(ApplicationError error, String title) {
        var problem = toProblemDetailFromApplicationError(error, title);
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }

    private static ProblemDetail createProblem(HttpStatusCode status, String code, String detail, String title) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        return problem;
    }

    private static URI toProblemTypeUri(String errorCode) {
        String slug = switch (errorCode) {
            case "VALIDATION_ERROR" -> "validation-error";
            case String s when s.endsWith("_FORBIDDEN") -> "forbidden";
            case String s when s.endsWith("_UNAUTHORIZED") -> "unauthorized";
            case String s when s.endsWith("_INVALID") || s.endsWith("_VALIDATION_ERROR") -> "validation-error";
            case "BUSINESS_RULE_VIOLATION" -> "business-rule-violation";
            case "UNEXPECTED_ERROR" -> "unexpected-error";
            case String s when s.endsWith("_NOT_FOUND") -> "not-found";
            case String s when s.endsWith("_CONFLICT") -> "conflict";
            default -> errorCode.toLowerCase(Locale.ROOT).replace('_', '-');
        };
        return URI.create(ERROR_TYPE_PREFIX + slug);
    }

    private static String toProblemTitle(String errorCode, HttpStatusCode status) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> "Validation Error";
            case "BUSINESS_RULE_VIOLATION" -> "Business Rule Violation";
            case String s when s.endsWith("_FORBIDDEN") -> "Forbidden";
            case String s when s.endsWith("_UNAUTHORIZED") -> "Unauthorized";
            case String s when s.endsWith("_INVALID") || s.endsWith("_VALIDATION_ERROR") -> "Validation Error";
            case String s when s.endsWith("_NOT_FOUND") -> "Resource Not Found";
            case String s when s.endsWith("_CONFLICT") -> "Resource Conflict";
            default -> status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Error";
        };
    }

    private static String toLocalizedMessageFromApplicationError(ApplicationError error) {
        String specificKey = toSpecificMessageKeyFromErrorCode(error.code());
        String specificMessage = toLocalizedMessageOrNull(specificKey, error.details(), toEntityNameFromErrorCode(error.code()));
        if (specificMessage != null) {
            return specificMessage;
        }

        String fallbackKey = toMessageKeyFromErrorCode(error.code());
        return toLocalizedMessageWithFallback(
                fallbackKey,
                error.message(),
                error.details(),
                toEntityNameFromErrorCode(error.code())
        );
    }

    private static String toSpecificMessageKeyFromErrorCode(String errorCode) {
        return "error.%s.message".formatted(errorCode.toLowerCase(Locale.ROOT).replace('_', '-'));
    }

    private static String toMessageKeyFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> "error.validation.message";
            case String s when s.endsWith("_INVALID") || s.endsWith("_VALIDATION_ERROR") -> "error.validation.message";
            case "BUSINESS_RULE_VIOLATION" -> "error.business-rule.message";
            case "UNEXPECTED_ERROR" -> "error.unexpected.message";
            case String s when s.endsWith("_NOT_FOUND") -> "error.not-found.message";
            case String s when s.endsWith("_CONFLICT") -> "error.conflict.message";
            default -> "error.generic.message";
        };
    }

    private static String toEntityNameFromErrorCode(String errorCode) {
        if (errorCode.endsWith("_NOT_FOUND")) {
            return errorCode.replace("_NOT_FOUND", "").toLowerCase(Locale.ROOT);
        }
        if (errorCode.endsWith("_CONFLICT")) {
            return errorCode.replace("_CONFLICT", "").toLowerCase(Locale.ROOT);
        }
        return "resource";
    }

    private static @Nullable String toLocalizedMessageOrNull(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, locale);
            if (!bundle.containsKey(key)) {
                return null;
            }
            String template = bundle.getString(key);
            return MessageFormat.format(template, args);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    private static String toLocalizedMessageWithFallback(String key, String fallback, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, locale);
            if (!bundle.containsKey(key)) {
                return fallback;
            }
            String template = bundle.getString(key);
            return MessageFormat.format(template, args);
        } catch (MissingResourceException ex) {
            return fallback;
        }
    }

    /**
     * Determines the appropriate HTTP status code for a given error code.
     *
     * @param errorCode the error code string (e.g., "PROFILE_NOT_FOUND", "VALIDATION_ERROR")
     * @return the corresponding HttpStatus
     */
    public static HttpStatusCode toStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "CHECKOUT_UNAVAILABLE", "CHECKOUT_PROVIDER", "RENTAL_LOCATION_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            case "RENTAL_LOCATION_NO_RESULTS" -> HttpStatus.NOT_FOUND;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case String s when s.endsWith("_INVALID") || s.endsWith("_VALIDATION_ERROR") -> HttpStatus.BAD_REQUEST;
            case String s when s.endsWith("_FORBIDDEN") -> HttpStatus.FORBIDDEN;
            case String s when s.endsWith("_UNAUTHORIZED") -> HttpStatus.UNAUTHORIZED;
            case String s when s.endsWith("_NOT_FOUND") -> HttpStatus.NOT_FOUND;
            case "BUSINESS_RULE_VIOLATION" -> HttpStatusCode.valueOf(422);
            case String s when s.endsWith("_CONFLICT") -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

package com.maquigest.platform.shared.interfaces.rest.transform;

import com.maquigest.platform.shared.application.result.ApplicationError;
import com.maquigest.platform.shared.application.result.Result;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.function.Function;

/**
 * Assembler that translates application Result values into HTTP responses.
 */
@NullMarked
public final class ResponseEntityAssembler {

    private ResponseEntityAssembler() {
    }

    /**
     * Read a command Result at the REST boundary without changing an established
     * bounded-context HTTP error contract (status, localized detail, code).
     * The transactional executor has already committed or rolled back before this call.
     */
    public static <T> T valueOrThrow(
            Result<T, ApplicationError> result,
            Function<ApplicationError, ? extends RuntimeException> toHttpException) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success -> success.value();
            case Result.Failure<T, ApplicationError> failure -> throw toHttpException.apply(failure.error());
        };
    }


    /**
     * Converts a Result into an HTTP response using the provided success resource assembler.
     * Failure responses are delegated to ProblemDetailFromApplicationErrorAssembler.
     *
     * @param result the application result
     * @param successResourceAssembler function that maps success value to response resource
     * @param successStatus HTTP status to use for successful responses
     * @param <T> success value type
     * @param <R> success response resource type
     * @return response entity for success or failure
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<T, ApplicationError> result,
            Function<T, R> successResourceAssembler,
            HttpStatusCode successStatus
    ) {
        return switch (result) {
            case Result.Success<T, ApplicationError> success ->
                    new ResponseEntity<>(successResourceAssembler.apply(success.value()), successStatus);
            case Result.Failure<T, ApplicationError> failure ->
                    ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(failure.error());
        };
    }
}

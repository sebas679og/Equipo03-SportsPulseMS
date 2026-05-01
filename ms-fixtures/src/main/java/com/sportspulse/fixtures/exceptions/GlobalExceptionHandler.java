package com.sportspulse.fixtures.exceptions;

import com.sportspulse.fixtures.constants.ErrorConstants;
import com.sportspulse.fixtures.dto.response.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

/**
 * Global Exception Handler for the SportsPulse Fixtures service.
 *
 * <p>Intercepts various exceptions thrown across the application and maps them to a standardized
 * {@link ErrorResponse} format. This ensures that the client receives meaningful status codes and
 * error messages instead of raw stack traces.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /** Handles specific domain failures where a requested fixture does not exist. */
  @ExceptionHandler(FixtureNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlerFixtureNotFound(FixtureNotFoundException ex) {
    ErrorResponse errorBody =
        ErrorResponse.builder()
            .error(ErrorConstants.Code.FIXTURE_NOT_FOUND)
            .message(ex.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
  }

  /**
   * Catches data type mismatches in path variables or query parameters (e.g., passing "abc" for a
   * Long ID).
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    String message =
        String.format("Parameter “%s” has an invalid value: '%s'", ex.getName(), ex.getValue());
    return buildError(ErrorConstants.Code.INVALID_PARAMETER, message, HttpStatus.BAD_REQUEST);
  }

  /** Processes validation failures from @Valid annotated objects. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
    return buildError(ErrorConstants.Code.VALIDATION_ERROR, message, HttpStatus.BAD_REQUEST);
  }

  /** Handles incorrect endpoint paths that bypass standard security/routing. */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
    return buildError(
        ErrorConstants.Code.RESOURCE_NOT_FOUND,
        ErrorConstants.Message.RESOURCE_NOT_FOUND,
        HttpStatus.NOT_FOUND);
  }

  /** Handles failures from upstream data providers (e.g., API-Football). */
  @ExceptionHandler(BadGatewayException.class)
  public ResponseEntity<ErrorResponse> handleBadGateway(BadGatewayException ex) {
    return buildError(
        ErrorConstants.Code.EXTERNAL_API_ERROR, ex.getMessage(), HttpStatus.BAD_GATEWAY);
  }

  /** Final safety net for unexpected runtime exceptions to prevent leaking internal details. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Unexpected error: ", ex);
    return buildError(
        ErrorConstants.Code.INTERNAL_ERROR,
        ErrorConstants.Message.INTERNAL_ERROR,
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Utility method to encapsulate ErrorResponse construction. */
  private ResponseEntity<ErrorResponse> buildError(
      String error, String message, HttpStatus status) {
    return ResponseEntity.status(status)
        .body(ErrorResponse.builder().error(error).message(message).build());
  }
}

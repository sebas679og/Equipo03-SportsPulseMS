package com.sportspulse.leagues.exceptions;

import com.sportspulse.leagues.dto.responses.ErrorResponse;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Global exception handler. */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomBadRequestException.class)
  public ResponseEntity<ErrorResponse> handleCustomBadRequestException(
      CustomBadRequestException ex) {
    return badRequest(ex.getMessage());
  }

  @ExceptionHandler(CustomBadGatewayException.class)
  public ResponseEntity<ErrorResponse> handleCustomBadGatewayException(
      CustomBadGatewayException ex) {
    return badGateway(ex.getMessage());
  }

  @ExceptionHandler(CustomNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCustomNotFoundException(CustomBadGatewayException ex) {
    return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(CustomServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleCustomServiceUnavailableException(
      CustomServiceUnavailableException ex) {
    return buildError(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }

  @ExceptionHandler(CustomTooManyRequestsException.class)
  public ResponseEntity<ErrorResponse> handleCustomTooManyRequestsException(
      CustomTooManyRequestsException ex) {
    return buildError(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
  }

  @ExceptionHandler(CustomUnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleCustomUnauthorizedException(
      CustomUnauthorizedException ex) {
    return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(ExternalDataInconsistencyException.class)
  public ResponseEntity<ErrorResponse> handleExternalDataInconsistency(
      ExternalDataInconsistencyException ex) {
    log.error("External data inconsistency: {}", ex.getMessage());
    return badGateway(ex.getMessage());
  }

  /**
   * Handles {@link MethodArgumentTypeMismatchException} thrown when a request parameter cannot be
   * converted to the expected type.
   *
   * <p>This typically occurs when a path variable or query parameter receives a value that does not
   * match the required data type (e.g., sending a string instead of a number).
   *
   * @param ex the exception containing details about the mismatched argument
   * @return a {@link ResponseEntity} with HTTP 400 (Bad Request) and a descriptive error message
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handlerMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    String message =
        String.format(
            "The parameter '%s' received an invalid value: '%s'", ex.getName(), ex.getValue());
    return badRequest(message);
  }

  /**
   * Handles validation exceptions thrown when request parameters fail to meet defined validation
   * constraints.
   *
   * <p>Extracts error messages from the {@link MethodArgumentNotValidException} and builds a
   * descriptive message by concatenating all validation errors. If a field-specific error is
   * detected, it provides a more detailed message indicating the invalid field.
   *
   * <p>Returns a {@link ResponseEntity} with a {@link ErrorResponse} body and {@code BAD_REQUEST}
   * status.
   *
   * @param ex the {@link MethodArgumentNotValidException} containing validation errors
   * @return a {@link ResponseEntity} with error details and {@code BAD_REQUEST} status
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  if (error instanceof FieldError fieldError) {
                    String defaultMessage = fieldError.getDefaultMessage();
                    if (defaultMessage != null && !defaultMessage.startsWith("Failed to convert")) {
                      return defaultMessage;
                    }
                    return "Invalid value for field '" + fieldError.getField() + "'";
                  }
                  return error.getDefaultMessage();
                })
            .collect(Collectors.joining(", "));
    return badRequest(message);
  }

  private ResponseEntity<ErrorResponse> badRequest(String description) {
    return buildError(HttpStatus.BAD_REQUEST, description);
  }

  private ResponseEntity<ErrorResponse> badGateway(String description) {
    return buildError(HttpStatus.BAD_GATEWAY, description);
  }

  private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String description) {
    return ResponseEntity.status(status)
        .body(
            ErrorResponse.builder()
                .code(status.value())
                .name(status.getReasonPhrase())
                .description(description)
                .build());
  }
}

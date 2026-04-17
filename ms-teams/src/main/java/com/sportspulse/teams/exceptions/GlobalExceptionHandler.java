package com.sportspulse.teams.exceptions;

import com.sportspulse.teams.dto.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class centralizes the handling of different exceptions thrown across the application,
 * transforming them into standardized HTTP responses with a consistent error structure.
 */
@SuppressWarnings("PMD.TooManyMethods")
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CustomUnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleCustomUnauthorizedException(
      CustomUnauthorizedException ex) {
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
  }

  @ExceptionHandler(CustomTooManyRequestsException.class)
  public ResponseEntity<ErrorResponse> handleCustomTooManyRequestsException(
      CustomTooManyRequestsException ex) {
    return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
  }

  @ExceptionHandler(CustomServiceUnavailableException.class)
  public ResponseEntity<ErrorResponse> handlerCustomServiceUnavailableException(
      CustomServiceUnavailableException ex) {
    return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
  }

  @ExceptionHandler(CustomNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlerCustomNotFoundException(CustomNotFoundException ex) {
    return notFound(ex.getMessage());
  }

  @ExceptionHandler(CustomBadGatewayException.class)
  public ResponseEntity<ErrorResponse> handlerCustomBadGatewayException(
      CustomBadGatewayException ex) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
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
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
    return notFound("The requested resource does not exist");
  }

  private ResponseEntity<ErrorResponse> notFound(String message) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, message);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String description) {
    return ResponseEntity.status(status)
        .body(
            ErrorResponse.builder()
                .code(status.value())
                .name(status.getReasonPhrase())
                .description(description)
                .build());
  }
}

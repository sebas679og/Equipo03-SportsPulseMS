package com.sportspulse.standings.exceptions;

import com.sportspulse.standings.dtos.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST controllers.
 *
 * <p>This class centralizes the handling of different exceptions thrown across the application,
 * transforming them into standardized HTTP responses with a consistent error structure.
 */
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
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(CustomBadGatewayException.class)
  public ResponseEntity<ErrorResponse> handlerCustomBadGatewayException(
      CustomBadGatewayException ex) {
    return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
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

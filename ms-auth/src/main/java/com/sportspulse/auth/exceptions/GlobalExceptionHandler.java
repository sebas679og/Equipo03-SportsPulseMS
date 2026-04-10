package com.sportspulse.auth.exceptions;

import com.sportspulse.auth.dto.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Global Exception Handler. */
@SuppressWarnings("PMD.TooManyMethods")
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceConflictException.class)
  public ResponseEntity<ErrorResponse> handleResourceConflictException(
      ResourceConflictException ex) {
    return conflict(ex.getMessage());
  }

  private ResponseEntity<ErrorResponse> conflict(String message) {
    return buildErrorResponse(HttpStatus.CONFLICT, message);
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

package com.sportspulse.auth.exceptions;

import com.sportspulse.auth.dto.responses.ErrorResponse;
import io.jsonwebtoken.JwtException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
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

  /**
   * Handle validation errors for request bodies annotated with @Valid. This method extracts
   * field-level error messages and compiles them into a single response for easier debugging.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> customMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String description =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  String message = error.getDefaultMessage();
                  if (message != null) {
                    message = message.replaceAll("\\s+", " ").trim();
                  }
                  if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + message;
                  }
                  return message;
                })
            .filter(msg -> msg != null && !msg.isBlank())
            .collect(Collectors.joining("; "));

    return badRequest(description);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
    return unauthorized(ex.getMessage());
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
    return unauthorized("Invalid or expired token");
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    return unauthorized("Authorization header is required");
  }

  private ResponseEntity<ErrorResponse> conflict(String message) {
    return buildErrorResponse(HttpStatus.CONFLICT, message);
  }

  private ResponseEntity<ErrorResponse> badRequest(String message) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  private ResponseEntity<ErrorResponse> unauthorized(String message) {
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, message);
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

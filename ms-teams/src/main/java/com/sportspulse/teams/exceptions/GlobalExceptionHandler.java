package com.sportspulse.teams.exceptions;

import com.sportspulse.teams.constants.Errors;
import com.sportspulse.teams.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Global exception handler for the Sports Pulse Teams microservice.
 *
 * <p>Intercepts exceptions thrown by the application and maps them to a consistent {@link
 * ErrorResponse} format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  /**
   * Handles missing required request parameters (HTTP 400).
   *
   * @param ex the exception thrown when a required parameter is absent.
   * @return a {@link ResponseEntity} with error details and status 400.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParams(
      MissingServletRequestParameterException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponse.of(
                Errors.Code.MISSING_PARAMETER,
                Errors.Message.MISSING_PARAMETER + ex.getParameterName()));
  }

  /**
   * Handles invalid parameter types (HTTP 400).
   *
   * @param ex the exception thrown when a parameter cannot be converted.
   * @return a {@link ResponseEntity} with error details and status 400.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorResponse.of(
                Errors.Code.INVALID_PARAMETER, Errors.Message.INVALID_PARAMETER + ex.getName()));
  }

  /**
   * Handles errors from the API-Football external service (HTTP 500).
   *
   * @param ex the exception thrown when the external service fails.
   * @return a {@link ResponseEntity} with error details and status 500.
   */
  @ExceptionHandler(WebClientResponseException.class)
  public ResponseEntity<ErrorResponse> handleWebClientError(WebClientResponseException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponse.of(
                Errors.Code.EXTERNAL_SERVICE_ERROR,
                Errors.Message.EXTERNAL_SERVICE_ERROR + ex.getStatusCode()));
  }

  /**
   * Handles any unexpected exception (HTTP 500).
   *
   * @param ex the unexpected exception.
   * @return a {@link ResponseEntity} with error details and status 500.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(Errors.Code.INTERNAL_ERROR, Errors.Message.INTERNAL_ERROR));
  }
}

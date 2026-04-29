package com.sportspulse.leagues.exceptions;

import com.sportspulse.leagues.dto.responses.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Global exception handler. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(LeagueNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleLeagueNotFound(LeagueNotFoundException ex) {
    return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(CustomBadGatewayException.class)
  public ResponseEntity<ErrorResponse> handleExternalApiError(CustomBadGatewayException ex) {
    return buildError(HttpStatus.BAD_GATEWAY, ex.getMessage());
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MethodArgumentNotValidException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
    return buildError(HttpStatus.BAD_REQUEST, "Parámetros de solicitud inválidos");
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    return buildError(HttpStatus.UNAUTHORIZED, "Authorization header es requerido");
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

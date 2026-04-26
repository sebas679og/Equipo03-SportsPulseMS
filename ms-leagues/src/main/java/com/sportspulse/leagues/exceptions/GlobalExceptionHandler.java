package com.sportspulse.leagues.exceptions;

import com.sportspulse.leagues.dto.responses.LeagueErrorResponse;
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
  public ResponseEntity<LeagueErrorResponse> handleLeagueNotFound(LeagueNotFoundException ex) {
    return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ExternalApiException.class)
  public ResponseEntity<LeagueErrorResponse> handleExternalApiError(ExternalApiException ex) {
    return buildError(HttpStatus.BAD_GATEWAY, "Error al consultar API-Football");
  }

  @ExceptionHandler({
    MethodArgumentTypeMismatchException.class,
    MethodArgumentNotValidException.class
  })
  public ResponseEntity<LeagueErrorResponse> handleBadRequest(Exception ex) {
    return buildError(HttpStatus.BAD_REQUEST, "Parámetros de solicitud inválidos");
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<LeagueErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    return buildError(HttpStatus.UNAUTHORIZED, "Authorization header es requerido");
  }

  private ResponseEntity<LeagueErrorResponse> buildError(HttpStatus status, String description) {
    return ResponseEntity.status(status)
        .body(
            LeagueErrorResponse.builder()
                .code(status.value())
                .name(status.getReasonPhrase())
                .description(description)
                .build());
  }
}

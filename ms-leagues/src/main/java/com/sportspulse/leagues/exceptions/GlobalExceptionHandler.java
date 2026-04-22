package com.sportspulse.leagues.exceptions;

import com.sportspulse.leagues.dto.responses.LeagueErrorResponse;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Global exception handler */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(LeagueNotFoundException.class)
  public ResponseEntity<LeagueErrorResponse> handleLeagueNotFound(LeagueNotFoundException ex) {
    return notFound("LEAGUE_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(ExternalApiException.class)
  public ResponseEntity<LeagueErrorResponse> handleExternalApiError(ExternalApiException ex) {
    return badGateway("EXTERNAL_API_ERROR", "Error al consultar API-Football");
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
  public ResponseEntity<LeagueErrorResponse> handleBadRequest(Exception ex) {
    return badRequest("BAD_REQUEST", "Parámetros de solicitud inválidos");
  }

  @ExceptionHandler(JwtException.class)
  public ResponseEntity<LeagueErrorResponse> handleJwtException(JwtException ex) {
    return unauthorized("UNAUTHORIZED", "Token JWT inválido o expirado");
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<LeagueErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    return unauthorized("UNAUTHORIZED", "Authorization header es requerido");
  }

  private ResponseEntity<LeagueErrorResponse> notFound(String error, String message) {
    return buildError(HttpStatus.NOT_FOUND, error, message);
  }

  private ResponseEntity<LeagueErrorResponse> badGateway(String error, String message) {
    return buildError(HttpStatus.BAD_GATEWAY, error, message);
  }

  private ResponseEntity<LeagueErrorResponse> badRequest(String error, String message) {
    return buildError(HttpStatus.BAD_REQUEST, error, message);
  }

  private ResponseEntity<LeagueErrorResponse> unauthorized(String error, String message) {
    return buildError(HttpStatus.UNAUTHORIZED, error, message);
  }

  private ResponseEntity<LeagueErrorResponse> buildError(
      HttpStatus status, String error, String message) {
    return ResponseEntity.status(status)
        .body(new LeagueErrorResponse(error, message, Instant.now()));
  }
}

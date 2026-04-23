package com.sportspulse.leagues.exceptions;

import com.sportspulse.leagues.dto.responses.LeagueErrorResponse;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("handleLeagueNotFound should return 404")
  void handleLeagueNotFound_shouldReturnNotFound() {
    ResponseEntity<LeagueErrorResponse> response =
        handler.handleLeagueNotFound(new LeagueNotFoundException());

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    Assertions.assertThat(response.getBody()).isNotNull();
    Assertions.assertThat(response.getBody().getError()).isEqualTo("LEAGUE_NOT_FOUND");
  }

  @Test
  @DisplayName("handleExternalApiError should return 502")
  void handleExternalApiError_shouldReturnBadGateway() {
    ResponseEntity<LeagueErrorResponse> response =
        handler.handleExternalApiError(new ExternalApiException("fail", new RuntimeException("cause")));

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    Assertions.assertThat(response.getBody()).isNotNull();
    Assertions.assertThat(response.getBody().getError()).isEqualTo("EXTERNAL_API_ERROR");
  }

  @Test
  @DisplayName("handleJwtException should return 401")
  void handleJwtException_shouldReturnUnauthorized() {
    ResponseEntity<LeagueErrorResponse> response =
        handler.handleJwtException(new JwtException("bad token"));

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    Assertions.assertThat(response.getBody()).isNotNull();
    Assertions.assertThat(response.getBody().getError()).isEqualTo("UNAUTHORIZED");
  }

  @Test
  @DisplayName("handleMissingHeader should return 401")
  void handleMissingHeader_shouldReturnUnauthorized() {
    MethodParameter parameter = Mockito.mock(MethodParameter.class);
    MissingRequestHeaderException ex = new MissingRequestHeaderException("Authorization", parameter);

    ResponseEntity<LeagueErrorResponse> response = handler.handleMissingHeader(ex);

    Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    Assertions.assertThat(response.getBody()).isNotNull();
    Assertions.assertThat(response.getBody().getMessage()).contains("Authorization header");
  }
}

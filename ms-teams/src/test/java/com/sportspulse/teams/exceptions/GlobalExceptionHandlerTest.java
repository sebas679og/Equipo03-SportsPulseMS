package com.sportspulse.teams.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sportspulse.teams.constants.Errors;
import com.sportspulse.teams.dto.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleMissingParams_shouldReturn400WithCorrectErrorCode() {

    MissingServletRequestParameterException ex =
        new MissingServletRequestParameterException("league", "Integer");

    ResponseEntity<ErrorResponse> result = handler.handleMissingParams(ex);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().error()).isEqualTo(Errors.Code.MISSING_PARAMETER);
    assertThat(result.getBody().message()).contains("league");
  }

  @Test
  void handleTypeMismatch_shouldReturn400WithCorrectErrorCode() {

    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getName()).thenReturn("season");

    ResponseEntity<ErrorResponse> result = handler.handleTypeMismatch(ex);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().error()).isEqualTo(Errors.Code.INVALID_PARAMETER);
    assertThat(result.getBody().message()).contains("season");
  }

  @Test
  void handleWebClientError_shouldReturn500WithCorrectErrorCode() {

    WebClientResponseException ex =
        WebClientResponseException.create(503, "Service Unavailable", null, null, null);

    ResponseEntity<ErrorResponse> result = handler.handleWebClientError(ex);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().error()).isEqualTo(Errors.Code.EXTERNAL_SERVICE_ERROR);
  }

  @Test
  void handleGenericError_shouldReturn500WithCorrectErrorCode() {

    Exception ex = new RuntimeException("Unexpected error");

    ResponseEntity<ErrorResponse> result = handler.handleGenericError(ex);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().error()).isEqualTo(Errors.Code.INTERNAL_ERROR);
    assertThat(result.getBody().message()).isEqualTo(Errors.Message.INTERNAL_ERROR);
  }
}

package com.sportspulse.gateway.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.gateway.dto.responses.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("JsonResponseWriter Tests")
class JsonResponseWriterTest {

  @Mock private ObjectMapper objectMapper;

  private JsonResponseWriter writer;

  @BeforeEach
  void setUp() {
    writer = new JsonResponseWriter(objectMapper);
  }

  @Test
  @DisplayName("write sets correct HTTP status on response")
  void write_setsCorrectHttpStatus() throws Exception {
    byte[] body =
        """
                {"code":429,"name":"TOO_MANY_REQUESTS","description":"Too many requests"}"""
            .getBytes();
    when(objectMapper.writeValueAsBytes(any(ErrorResponse.class))).thenReturn(body);

    MockServerWebExchange exchange = exchangeFor("/api/test");

    StepVerifier.create(writer.write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests"))
        .verifyComplete();

    assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  @DisplayName("write sets Content-Type to application/json")
  void write_setsJsonContentType() throws Exception {
    byte[] body = "{}".getBytes();
    when(objectMapper.writeValueAsBytes(any())).thenReturn(body);

    MockServerWebExchange exchange = exchangeFor("/api/test");

    StepVerifier.create(writer.write(exchange, HttpStatus.FORBIDDEN, "Access denied"))
        .verifyComplete();

    assertThat(exchange.getResponse().getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_JSON);
  }

  @Test
  @DisplayName("write serializes ErrorResponse with correct fields")
  void write_serializesErrorResponseWithCorrectFields() throws Exception {
    byte[] body = "{}".getBytes();
    when(objectMapper.writeValueAsBytes(any(ErrorResponse.class))).thenReturn(body);

    MockServerWebExchange exchange = exchangeFor("/api/test");

    StepVerifier.create(writer.write(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized"))
        .verifyComplete();

    var captor = org.mockito.ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsBytes(captor.capture());
    ErrorResponse captured = captor.getValue();

    assertThat(captured.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(captured.getName()).isEqualTo(HttpStatus.UNAUTHORIZED.name());
    assertThat(captured.getDescription()).isEqualTo("Unauthorized");
  }

  @Test
  @DisplayName("write completes normally when response body is written successfully")
  void write_completesOnSuccess() throws Exception {
    when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());

    MockServerWebExchange exchange = exchangeFor("/api/test");

    StepVerifier.create(writer.write(exchange, HttpStatus.OK, "OK")).verifyComplete();
  }

  @Test
  @DisplayName("write handles JsonProcessingException by invoking fallback path")
  void write_onSerializationFailure_invokesFallback() throws Exception {
    // First call throws, second call (fallback) also throws to test final fallback
    JsonProcessingException jpex = mock(JsonProcessingException.class);
    when(objectMapper.writeValueAsBytes(any())).thenThrow(jpex).thenReturn("{}".getBytes());

    MockServerWebExchange exchange = exchangeFor("/api/test");

    StepVerifier.create(writer.write(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Error"))
        .verifyComplete();

    // writeValueAsBytes called at least twice (primary + fallback)
    verify(objectMapper, atLeast(2)).writeValueAsBytes(any());
  }

  @Test
  @DisplayName("write returns Mono.empty() when response is already committed")
  void write_whenResponseAlreadyCommitted_returnsEmpty() throws Exception {
    when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());

    MockServerWebExchange exchange = exchangeFor("/api/test");
    // Commit the response first
    exchange.getResponse().setComplete().block();

    StepVerifier.create(writer.write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Limit exceeded"))
        .verifyComplete();
  }

  @Test
  @DisplayName("write with 503 status writes correct error code in body")
  void write_503_writesCorrectErrorCode() throws Exception {
    when(objectMapper.writeValueAsBytes(any(ErrorResponse.class))).thenReturn("{}".getBytes());

    MockServerWebExchange exchange = exchangeFor("/fallback/503");

    StepVerifier.create(
            writer.write(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Service unavailable"))
        .verifyComplete();

    var captor = org.mockito.ArgumentCaptor.forClass(ErrorResponse.class);
    verify(objectMapper).writeValueAsBytes(captor.capture());
    assertThat(captor.getValue().getCode()).isEqualTo(503);
  }

  // ─── helpers ──────────────────────────────────────────────────────────────

  private MockServerWebExchange exchangeFor(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
  }
}

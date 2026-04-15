package com.sportspulse.gateway.exceptions;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalErrorWebExceptionHandler Tests")
class GlobalErrorWebExceptionHandlerTest {

  @Mock private ErrorAttributes errorAttributes;
  @Mock private ObjectMapper objectMapper;
  @Mock private ServerRequest serverRequest;
  @Mock private ServerWebExchange exchange;
  @Mock private ServerHttpResponse httpResponse;
  @Mock private DataBufferFactory bufferFactory;

  private GlobalErrorWebExceptionHandler handler;

  @BeforeEach
  void setUp() {
    WebProperties webProperties = new WebProperties();
    ApplicationContext ctx = new AnnotationConfigApplicationContext();

    handler = new GlobalErrorWebExceptionHandler(errorAttributes, webProperties, ctx, objectMapper);
  }

  // ─── resolveDescription ──────────────────────────────────────────────────

  @Test
  @DisplayName("resolveDescription returns correct message for NOT_FOUND")
  void resolveDescription_notFound() {
    assertThat(handler.resolveDescription(HttpStatus.NOT_FOUND, new RuntimeException()))
        .isEqualTo("The requested resource was not found.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for METHOD_NOT_ALLOWED")
  void resolveDescription_methodNotAllowed() {
    assertThat(handler.resolveDescription(HttpStatus.METHOD_NOT_ALLOWED, new RuntimeException()))
        .isEqualTo("HTTP method not allowed for this endpoint.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for UNAUTHORIZED")
  void resolveDescription_unauthorized() {
    assertThat(handler.resolveDescription(HttpStatus.UNAUTHORIZED, new RuntimeException()))
        .isEqualTo("Authentication is required to access this resource.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for FORBIDDEN")
  void resolveDescription_forbidden() {
    assertThat(handler.resolveDescription(HttpStatus.FORBIDDEN, new RuntimeException()))
        .isEqualTo("You do not have permission to access this resource.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for TOO_MANY_REQUESTS")
  void resolveDescription_tooManyRequests() {
    assertThat(handler.resolveDescription(HttpStatus.TOO_MANY_REQUESTS, new RuntimeException()))
        .isEqualTo("Too many requests, please try again later.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for BAD_REQUEST")
  void resolveDescription_badRequest() {
    assertThat(handler.resolveDescription(HttpStatus.BAD_REQUEST, new RuntimeException()))
        .isEqualTo("The request is malformed or contains invalid parameters.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for SERVICE_UNAVAILABLE")
  void resolveDescription_serviceUnavailable() {
    assertThat(handler.resolveDescription(HttpStatus.SERVICE_UNAVAILABLE, new RuntimeException()))
        .isEqualTo("The service is temporarily unavailable. Please try again later.");
  }

  @Test
  @DisplayName("resolveDescription returns correct message for GATEWAY_TIMEOUT")
  void resolveDescription_gatewayTimeout() {
    assertThat(handler.resolveDescription(HttpStatus.GATEWAY_TIMEOUT, new RuntimeException()))
        .isEqualTo("The upstream service did not respond in time.");
  }

  @Test
  @DisplayName("resolveDescription returns error message for 5xx with message")
  void resolveDescription_5xx_withMessage_returnsErrorMessage() {
    assertThat(
            handler.resolveDescription(
                HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException("boom")))
        .isEqualTo("boom");
  }

  @Test
  @DisplayName("resolveDescription returns reason phrase for 5xx with null message")
  void resolveDescription_5xx_nullMessage_returnsReasonPhrase() {
    assertThat(
            handler.resolveDescription(
                HttpStatus.INTERNAL_SERVER_ERROR, new RuntimeException((String) null)))
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
  }

  @Test
  @DisplayName("resolveDescription returns reason phrase for non-5xx unmatched status")
  void resolveDescription_unknown_non5xx_returnsReasonPhrase() {
    assertThat(handler.resolveDescription(HttpStatus.CONFLICT, new RuntimeException()))
        .isEqualTo(HttpStatus.CONFLICT.getReasonPhrase());
  }

  // ─── resolveStatusCode ───────────────────────────────────────────────────

  @Test
  @DisplayName("resolveStatusCode returns Integer value when attribute is Integer")
  void resolveStatusCode_integer_returnsCode() {
    assertThat(handler.resolveStatusCode(Map.of("status", 404))).isEqualTo(404);
  }

  @Test
  @DisplayName("resolveStatusCode parses String value correctly")
  void resolveStatusCode_string_parsesCorrectly() {
    assertThat(handler.resolveStatusCode(Map.of("status", "503"))).isEqualTo(503);
  }

  @Test
  @DisplayName("resolveStatusCode returns 500 when status is not parseable")
  void resolveStatusCode_unparseable_returns500() {
    assertThat(handler.resolveStatusCode(Map.of("status", "not-a-number"))).isEqualTo(500);
  }

  @Test
  @DisplayName("resolveStatusCode returns 500 when status key is absent")
  void resolveStatusCode_missingKey_returns500() {
    assertThat(handler.resolveStatusCode(Collections.emptyMap())).isEqualTo(500);
  }

  // ─── renderErrorResponse ─────────────────────────────────────────────────

  @Test
  @DisplayName("renderErrorResponse writes JSON body on success")
  void renderErrorResponse_writesJsonBody() throws Exception {
    mockExchangeAndResponse();
    when(httpResponse.bufferFactory()).thenReturn(bufferFactory);
    when(bufferFactory.wrap(any(byte[].class))).thenReturn(mock(DataBuffer.class));
    when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(Map.of("status", 404));
    when(errorAttributes.getError(any())).thenReturn(new RuntimeException("not found"));
    when(objectMapper.writeValueAsBytes(any())).thenReturn("{\"code\":404}".getBytes());
    when(httpResponse.writeWith(any())).thenReturn(Mono.empty());

    StepVerifier.create(handler.renderErrorResponse(serverRequest)).verifyComplete();

    verify(httpResponse).setStatusCode(HttpStatus.NOT_FOUND);
    verify(httpResponse).writeWith(any());
  }

  @Test
  @DisplayName("renderErrorResponse calls setComplete when serialization fails")
  void renderErrorResponse_serializationFailure_callsSetComplete() throws Exception {
    mockExchangeAndResponse();
    // sin bufferFactory ni writeWith — no se llegan a usar
    when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(Map.of("status", 500));
    when(errorAttributes.getError(any())).thenReturn(new RuntimeException("boom"));
    when(objectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);
    when(httpResponse.setComplete()).thenReturn(Mono.empty());

    StepVerifier.create(handler.renderErrorResponse(serverRequest)).verifyComplete();

    verify(httpResponse).setComplete();
  }

  @Test
  @DisplayName("renderErrorResponse falls back to INTERNAL_SERVER_ERROR for unknown status code")
  void renderErrorResponse_unknownStatusCode_fallsBackTo500() throws Exception {
    mockExchangeAndResponse();
    when(httpResponse.bufferFactory()).thenReturn(bufferFactory);
    when(bufferFactory.wrap(any(byte[].class))).thenReturn(mock(DataBuffer.class));
    when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(Map.of("status", 999));
    when(errorAttributes.getError(any())).thenReturn(new RuntimeException("unknown"));
    when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());
    when(httpResponse.writeWith(any())).thenReturn(Mono.empty());

    StepVerifier.create(handler.renderErrorResponse(serverRequest)).verifyComplete();

    verify(httpResponse).setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // ─── helpers ─────────────────────────────────────────────────────────────

  private void mockExchangeAndResponse() {
    when(serverRequest.exchange()).thenReturn(exchange);
    when(serverRequest.path()).thenReturn("/api/test");
    when(exchange.getResponse()).thenReturn(httpResponse);
    when(httpResponse.getHeaders()).thenReturn(new HttpHeaders());
  }
}

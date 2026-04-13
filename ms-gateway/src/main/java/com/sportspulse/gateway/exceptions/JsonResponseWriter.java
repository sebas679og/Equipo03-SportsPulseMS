package com.sportspulse.gateway.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.gateway.dto.responses.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** JsonResponseWriter Writes standardized JSON error responses for WebFlux exchanges. */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonResponseWriter {

  private final ObjectMapper objectMapper;

  /**
   * Writes a JSON error response to the given exchange. Serializes the error details and handles
   * serialization failures gracefully.
   *
   * @param exchange the ServerWebExchange to write to
   * @param status the HTTP status to set
   * @param message the error message description
   * @return a Mono that completes when the response is written
   */
  public Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
    return Mono.fromCallable(() -> serialize(status, message))
        .flatMap(body -> writeResponse(exchange, status, body))
        .onErrorResume(
            JsonProcessingException.class,
            ex -> {
              if (log.isErrorEnabled()) {
                log.error(
                    "[JsonResponseWriter] Serialization failed — status={} message='{}': {}",
                    status,
                    message,
                    ex.getMessage());
              }
              return fallback(exchange, status, message);
            });
  }

  private byte[] serialize(HttpStatus status, String message) throws JsonProcessingException {
    return objectMapper.writeValueAsBytes(
        ErrorResponse.builder()
            .code(status.value())
            .name(status.name())
            .description(message)
            .build());
  }

  private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatus status, byte[] body) {
    ServerHttpResponse response = exchange.getResponse();

    if (response.isCommitted()) {
      if (log.isWarnEnabled()) {
        log.warn("[JsonResponseWriter] Response already committed — status={}", status);
      }
      return Mono.empty();
    }

    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
  }

  private Mono<Void> fallback(ServerWebExchange exchange, HttpStatus status, String message) {
    try {
      byte[] body =
          objectMapper.writeValueAsBytes(
              ErrorResponse.builder()
                  .code(status.value())
                  .name(status.name())
                  .description(message)
                  .build());
      return writeResponse(exchange, status, body);

    } catch (JsonProcessingException ex) {
      if (log.isErrorEnabled()) {
        log.error("[JsonResponseWriter] Fallback serialization also failed: {}", ex.getMessage());
      }
      return exchange.getResponse().setComplete();
    }
  }
}

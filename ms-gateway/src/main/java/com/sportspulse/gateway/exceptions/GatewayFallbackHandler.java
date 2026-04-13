package com.sportspulse.gateway.exceptions;

import com.sportspulse.gateway.dto.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/** GatewayFallbackHandler Handles fallback responses when a gateway route is unavailable. */
@Slf4j
@Component
public class GatewayFallbackHandler {

  /**
   * Returns a standardized error response when a service is unavailable. Logs the route and path
   * information for troubleshooting.
   *
   * @param request the ServerRequest triggering the fallback
   * @return a Mono containing the ServerResponse with HTTP 503 status
   */
  public Mono<ServerResponse> serviceUnavailable(ServerRequest request) {
    Route route = request.exchange().getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
    String routeId = route != null ? route.getId() : "unknown";

    if (log.isErrorEnabled()) {
      log.error(
          "[GatewayFallbackHandler] Service unavailable — route='{}' path='{}'",
          routeId,
          request.path());
    }

    return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
            ErrorResponse.builder()
                .code(HttpStatus.SERVICE_UNAVAILABLE.value())
                .name(HttpStatus.SERVICE_UNAVAILABLE.name())
                .description("The service is temporarily unavailable. Please try again later.")
                .build());
  }
}

package com.sportspulse.gateway.filter;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * InternalRouteBlockerFilter Blocks access to internal gateway routes by returning a standardized
 * error response.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalRouteBlockerFilter implements GlobalFilter, Ordered {

  private static final List<String> INTERNAL_PREFIXES =
      List.of(ApiPathsServices.Auth.VALIDATE_TOKEN);

  private final JsonResponseWriter jsonResponseWriter;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getPath().value();

    if (INTERNAL_PREFIXES.stream().anyMatch(path::startsWith)) {
      log.warn("[InternalRouteBlockerFilter] Blocked internal route access — path='{}'", path);
      return jsonResponseWriter.write(exchange, HttpStatus.NOT_FOUND, "Route not found: " + path);
    }

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}

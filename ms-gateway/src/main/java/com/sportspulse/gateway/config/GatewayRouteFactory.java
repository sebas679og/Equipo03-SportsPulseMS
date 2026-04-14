package com.sportspulse.gateway.config;

import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * GatewayRouteFactory Builds and applies standard and brute-force rate limiting filters for gateway
 * routes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayRouteFactory {

  private final FixedWindowRateLimiter fixedWindowRateLimiter;
  private final KeyResolver keyResolver;
  private final JsonResponseWriter responseWriter;

  private static final FixedWindowRateLimiter.Config DEFAULT_CONFIG =
          new FixedWindowRateLimiter.Config(); // 60 req / 60s

  private static final FixedWindowRateLimiter.Config BRUTE_FORCE_CONFIG =
          buildConfig(5, 60); // 5 req / 60s

  private static FixedWindowRateLimiter.Config buildConfig(int max, long window) {
    FixedWindowRateLimiter.Config c = new FixedWindowRateLimiter.Config();
    c.setMaxRequests(max);
    c.setWindowSeconds(window);
    return c;
  }

  public Function<GatewayFilterSpec, UriSpec> applyStandardFilters(String circuitName) {
    return applyFilters(circuitName, DEFAULT_CONFIG);
  }

  public Function<GatewayFilterSpec, UriSpec> applyBruteForceFilters(String circuitName) {
    return applyFilters(circuitName, BRUTE_FORCE_CONFIG);
  }

  private Function<GatewayFilterSpec, UriSpec> applyFilters(
          String circuitName, FixedWindowRateLimiter.Config config) {
    return f ->
            f.filter(rateLimitFilter(config))
                    .circuitBreaker(c -> c.setName(circuitName).setFallbackUri("forward:/fallback/503"));
  }

  private GatewayFilter rateLimitFilter(FixedWindowRateLimiter.Config config) {
    return (exchange, chain) ->
            keyResolver
                    .resolve(exchange)
                    .flatMap(
                            key ->
                                    fixedWindowRateLimiter.isAllowed(
                                            Optional.ofNullable(
                                                            (Route) exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR))
                                                    .map(Route::getId)
                                                    .orElse("default"),
                                            key,
                                            config))
                    .flatMap(
                            response -> {
                              if (response.isAllowed()) {
                                return chain.filter(exchange);
                              }
                              String retryAfter = resolveRetryAfter(response);
                              String message = retryAfter != null
                                      ? "Too many requests, please try again in " + retryAfter
                                      : "Too many requests, please try again later";
                              return responseWriter.write(exchange, HttpStatus.TOO_MANY_REQUESTS, message);
                            });
  }

  private String resolveRetryAfter(RateLimiter.Response response) {
    try {
      String resetIn = response.getHeaders().get("X-RateLimit-Reset-In");
      return (resetIn != null && !resetIn.isBlank()) ? resetIn : null;
    } catch (Exception e) {
      return null;
    }
  }
}

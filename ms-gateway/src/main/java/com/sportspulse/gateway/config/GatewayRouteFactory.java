package com.sportspulse.gateway.config;

import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
public class GatewayRouteFactory {

  @Qualifier("defaultRateLimiter")
  private final RedisRateLimiter defaultRateLimiter;

  @Qualifier("bruteForceRateLimiter")
  private final RedisRateLimiter bruteForceRateLimiter;

  private final KeyResolver keyResolver;
  private final JsonResponseWriter responseWriter;

  /**
   * Constructs a GatewayRouteFactory with the required rate limiters, key resolver, and response
   * writer.
   *
   * @param defaultRateLimiter the default rate limiter for standard requests
   * @param bruteForceRateLimiter the rate limiter for brute-force protection
   * @param keyResolver resolves keys for rate limiting
   * @param responseWriter writes JSON responses for errors
   */
  public GatewayRouteFactory(
      @Qualifier("defaultRateLimiter") RedisRateLimiter defaultRateLimiter,
      @Qualifier("bruteForceRateLimiter") RedisRateLimiter bruteForceRateLimiter,
      KeyResolver keyResolver,
      JsonResponseWriter responseWriter) {
    this.defaultRateLimiter = defaultRateLimiter;
    this.bruteForceRateLimiter = bruteForceRateLimiter;
    this.keyResolver = keyResolver;
    this.responseWriter = responseWriter;
  }

  public Function<GatewayFilterSpec, UriSpec> applyStandardFilters(String circuitName) {
    return applyFilters(circuitName, defaultRateLimiter);
  }

  public Function<GatewayFilterSpec, UriSpec> applyBruteForceFilters(String circuitName) {
    return applyFilters(circuitName, bruteForceRateLimiter);
  }

  private Function<GatewayFilterSpec, UriSpec> applyFilters(
      String circuitName, RedisRateLimiter rateLimiter) {
    return f ->
        f.filter(rateLimitFilter(rateLimiter))
            .circuitBreaker(c -> c.setName(circuitName).setFallbackUri("forward:/fallback/503"));
  }

  private GatewayFilter rateLimitFilter(RedisRateLimiter rateLimiter) {
    return (exchange, chain) ->
        keyResolver
            .resolve(exchange)
            .flatMap(
                key ->
                    rateLimiter.isAllowed(
                        Optional.ofNullable(
                                (Route)
                                    exchange.getAttribute(
                                        ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR))
                            .map(Route::getId)
                            .orElse("default"),
                        key))
            .flatMap(
                response -> {
                  if (response.isAllowed()) {
                    return chain.filter(exchange);
                  }
                  String retryAfter = resolveRetryAfter(response);
                  String message =
                      retryAfter != null
                          ? "Too many requests, please try again in " + retryAfter
                          : "Too many requests, please try again later";
                  return responseWriter.write(exchange, HttpStatus.TOO_MANY_REQUESTS, message);
                });
  }

  private String resolveRetryAfter(RateLimiter.Response response) {
    try {
      Map<String, String> headers = response.getHeaders();

      String remainingStr = headers.get("X-RateLimit-Remaining");
      String requestedStr = headers.get("X-RateLimit-Requested-Tokens");
      String replenishStr = headers.get("X-RateLimit-Replenish-Rate");

      if (remainingStr == null || requestedStr == null || replenishStr == null) {
        return null;
      }

      int remaining = Integer.parseInt(remainingStr.trim());
      int requested = Integer.parseInt(requestedStr.trim());
      int replenishRate = Integer.parseInt(replenishStr.trim());

      int tokensNeeded = requested - remaining; // cuánto falta realmente
      if (tokensNeeded <= 0) {
        return "0s";
      }

      long waitSeconds = (long) Math.ceil((double) tokensNeeded / replenishRate);
      return waitSeconds + "s";
    } catch (NumberFormatException e) {
      if (log.isWarnEnabled()) {
        log.warn("[GatewayRouteFactory] Could not parse X-RateLimit-Reset header");
      }
      return null;
    }
  }
}

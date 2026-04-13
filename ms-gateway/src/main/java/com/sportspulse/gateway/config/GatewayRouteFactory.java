package com.sportspulse.gateway.config;

import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
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
                  return responseWriter.write(
                      exchange,
                      HttpStatus.TOO_MANY_REQUESTS,
                      "Too many requests, please try again later");
                });
  }
}

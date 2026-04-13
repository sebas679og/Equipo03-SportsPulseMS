package com.sportspulse.gateway.config;

import com.sportspulse.gateway.config.constants.InternalHeaders;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/** RateLimiterConfig Configures rate limiting components for the gateway. */
@Configuration
public class RateLimiterConfig {

  /**
   * Resolves a key for rate limiting based on the client IP address. Uses the X-Forwarded-For
   * header if available, otherwise falls back to the remote address of the request.
   *
   * @return a KeyResolver that extracts the client IP
   */
  @Bean
  @Primary
  public KeyResolver ipKeyResolver() {
    return exchange -> {
      String forwarded =
          exchange.getRequest().getHeaders().getFirst(InternalHeaders.CLIENT_IP_HEADER);
      if (forwarded != null && !forwarded.isBlank()) {
        return Mono.just(forwarded.split(",")[0].trim());
      }
      return Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
          .map(addr -> addr.getAddress().getHostAddress())
          .defaultIfEmpty("unknown");
    };
  }

  @Primary
  @Bean("defaultRateLimiter")
  public RedisRateLimiter defaultRedisRateLimiter() {
    return new RedisRateLimiter(1, 60, 1);
  }

  @Bean("bruteForceRateLimiter")
  public RedisRateLimiter bruteForceRateLimiter() {
    return new RedisRateLimiter(1, 10, 6);
  }
}

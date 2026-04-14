package com.sportspulse.gateway.config;

import com.sportspulse.gateway.config.constants.InternalHeaders;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/** KeyResolverConfig Configures key resolvers for rate limiting in the gateway. */
@Configuration
public class KeyResolverConfig {

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
}

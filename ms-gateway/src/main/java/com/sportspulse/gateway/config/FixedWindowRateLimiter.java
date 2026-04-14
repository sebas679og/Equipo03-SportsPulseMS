package com.sportspulse.gateway.config;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** FixedWindowRateLimiter Implements a fixed-window rate limiting strategy using Redis. */
@Primary
@Component
@RequiredArgsConstructor
public class FixedWindowRateLimiter implements RateLimiter<FixedWindowRateLimiter.Config> {

  private static final String SCRIPT =
      """
      local count = redis.call('INCR', KEYS[1])
      if count == 1 then
        redis.call('EXPIRE', KEYS[1], ARGV[1])
      end
      local ttl = redis.call('TTL', KEYS[1])
      return {count, ttl}
      """;

  private final ReactiveRedisTemplate<String, String> redisTemplate;

  @Override
  public Mono<Response> isAllowed(String routeId, String id) {
    throw new UnsupportedOperationException("Use isAllowed(id, config)");
  }

  /**
   * Determines whether a request is allowed under the fixed-window rate limiting strategy. Executes
   * a Redis script to track request counts and TTL within the configured window.
   *
   * @param routeId the route identifier
   * @param id the client identifier
   * @param config the rate limiter configuration (window size and max requests)
   * @return a Mono containing the rate limiting response with headers
   */
  public Mono<Response> isAllowed(String routeId, String id, Config config) {
    String key = "fixed_window:" + routeId + ":" + id;
    long windowSeconds = config.getWindowSeconds();
    int maxRequests = config.getMaxRequests();

    ByteBuffer keyBuffer = ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8));
    ByteBuffer windowBuffer =
        ByteBuffer.wrap(String.valueOf(windowSeconds).getBytes(StandardCharsets.UTF_8));

    return redisTemplate
        .execute(
            connection ->
                connection
                    .scriptingCommands()
                    .eval(
                        ByteBuffer.wrap(SCRIPT.getBytes(StandardCharsets.UTF_8)),
                        ReturnType.MULTI,
                        1,
                        keyBuffer,
                        windowBuffer))
        .next()
        .map(
            result -> {
              List<?> list = (List<?>) result;
              long count = (Long) list.get(0);
              long ttl = (Long) list.get(1);
              final boolean allowed = count <= maxRequests;

              Map<String, String> headers = new ConcurrentHashMap<>();
              headers.put(
                  "X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - count)));
              headers.put("X-RateLimit-Requested-Tokens", "1");
              headers.put("X-RateLimit-Replenish-Rate", String.valueOf(maxRequests));
              headers.put("X-RateLimit-Reset-In", ttl + "s");

              return new Response(allowed, headers);
            });
  }

  @Override
  public Map<String, Config> getConfig() {
    return Collections.emptyMap();
  }

  @Override
  public Class<Config> getConfigClass() {
    return Config.class;
  }

  @Override
  public Config newConfig() {
    return new Config();
  }

  /** Config Configuration properties for the FixedWindowRateLimiter. */
  @Data
  public static class Config {
    private int maxRequests = 60;
    private long windowSeconds = 60;
  }
}

package com.sportspulse.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.connection.ReactiveScriptingCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.ReactiveRedisCallback;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("FixedWindowRateLimiter Tests")
class FixedWindowRateLimiterTest {

  @Mock private ReactiveRedisTemplate<String, String> redisTemplate;
  @Mock private ReactiveRedisConnection connection;
  @Mock private ReactiveScriptingCommands scriptingCommands;

  private FixedWindowRateLimiter rateLimiter;

  private static final String ROUTE_ID = "ms-auth";
  private static final String CLIENT_IP = "127.0.0.1";

  @BeforeEach
  void setUp() {
    rateLimiter = new FixedWindowRateLimiter(redisTemplate);
  }

  private void mockRedisConnection() {
    when(redisTemplate.execute(any(ReactiveRedisCallback.class)))
        .thenAnswer(
            invocation -> {
              ReactiveRedisCallback<?> callback = invocation.getArgument(0);
              return callback.doInRedis(connection);
            });
    when(connection.scriptingCommands()).thenReturn(scriptingCommands);
  }

  // ─── isAllowed(routeId, id) — unsupported ────────────────────────────────

  @Test
  @DisplayName("isAllowed(routeId, id) throws UnsupportedOperationException")
  void isAllowed_twoArgs_throwsUnsupportedOperation() {
    assertThatThrownBy(() -> rateLimiter.isAllowed(ROUTE_ID, CLIENT_IP))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Use isAllowed(id, config)");
  }

  // ─── isAllowed — request allowed ─────────────────────────────────────────

  @Test
  @DisplayName("request within limit is allowed and headers are set correctly")
  void isAllowed_withinLimit_returnsAllowedWithCorrectHeaders() {
    mockRedisConnection();
    mockRedisResult(3L, 45L);

    StepVerifier.create(rateLimiter.isAllowed(ROUTE_ID, CLIENT_IP, configWith(5, 60)))
        .assertNext(
            response -> {
              assertThat(response.isAllowed()).isTrue();
              assertThat(response.getHeaders())
                  .containsEntry("X-RateLimit-Remaining", "2")
                  .containsEntry("X-RateLimit-Requested-Tokens", "1")
                  .containsEntry("X-RateLimit-Replenish-Rate", "5")
                  .containsEntry("X-RateLimit-Reset-In", "45s");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("request exactly at limit is allowed")
  void isAllowed_exactlyAtLimit_returnsAllowed() {
    mockRedisConnection();
    mockRedisResult(5L, 30L);

    StepVerifier.create(rateLimiter.isAllowed(ROUTE_ID, CLIENT_IP, configWith(5, 60)))
        .assertNext(
            response -> {
              assertThat(response.isAllowed()).isTrue();
              assertThat(response.getHeaders()).containsEntry("X-RateLimit-Remaining", "0");
            })
        .verifyComplete();
  }

  // ─── isAllowed — request denied ──────────────────────────────────────────

  @Test
  @DisplayName("request over limit is denied and remaining is clamped to 0")
  void isAllowed_overLimit_returnsDeniedAndRemainingIsZero() {
    mockRedisConnection();
    mockRedisResult(6L, 55L);

    StepVerifier.create(rateLimiter.isAllowed(ROUTE_ID, CLIENT_IP, configWith(5, 60)))
        .assertNext(
            response -> {
              assertThat(response.isAllowed()).isFalse();
              assertThat(response.getHeaders())
                  .containsEntry("X-RateLimit-Remaining", "0")
                  .containsEntry("X-RateLimit-Reset-In", "55s");
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("retryAfter reflects TTL from Redis when denied")
  void isAllowed_denied_retryAfterMatchesTtl() {
    mockRedisConnection();
    mockRedisResult(10L, 42L);

    StepVerifier.create(rateLimiter.isAllowed(ROUTE_ID, CLIENT_IP, configWith(3, 60)))
        .assertNext(
            response -> {
              assertThat(response.isAllowed()).isFalse();
              assertThat(response.getHeaders()).containsEntry("X-RateLimit-Reset-In", "42s");
            })
        .verifyComplete();
  }

  // ─── Redis key format ─────────────────────────────────────────────────────

  @Test
  @DisplayName("Redis key is built as fixed_window:{routeId}:{clientId}")
  void isAllowed_redisKeyFormat_isCorrect() {
    mockRedisConnection();

    ArgumentCaptor<ByteBuffer> keyCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

    when(scriptingCommands.eval(any(), eq(ReturnType.MULTI), eq(1), keyCaptor.capture(), any()))
        .thenReturn(Flux.just(List.of(1L, 60L)));

    StepVerifier.create(rateLimiter.isAllowed("ms-auth-login", "10.0.0.1", configWith(10, 60)))
        .assertNext(r -> assertThat(r.isAllowed()).isTrue())
        .verifyComplete();

    String capturedKey = StandardCharsets.UTF_8.decode(keyCaptor.getValue()).toString();
    assertThat(capturedKey).isEqualTo("fixed_window:ms-auth-login:10.0.0.1");
  }

  // ─── metadata methods ─────────────────────────────────────────────────────

  @Test
  @DisplayName("getConfig returns empty map")
  void getConfig_returnsEmptyMap() {
    assertThat(rateLimiter.getConfig()).isEmpty();
  }

  @Test
  @DisplayName("getConfigClass returns Config class")
  void getConfigClass_returnsConfigClass() {
    assertThat(rateLimiter.getConfigClass()).isEqualTo(FixedWindowRateLimiter.Config.class);
  }

  @Test
  @DisplayName("newConfig returns Config with default values")
  void newConfig_returnsDefaultConfig() {
    FixedWindowRateLimiter.Config config = rateLimiter.newConfig();
    assertThat(config.getMaxRequests()).isEqualTo(60);
    assertThat(config.getWindowSeconds()).isEqualTo(60);
  }

  // ─── helpers ──────────────────────────────────────────────────────────────

  private void mockRedisResult(long count, long ttl) {
    when(scriptingCommands.eval(any(), eq(ReturnType.MULTI), eq(1), any(), any()))
        .thenReturn(Flux.just(List.of(count, ttl)));
  }

  private FixedWindowRateLimiter.Config configWith(int maxRequests, long windowSeconds) {
    FixedWindowRateLimiter.Config config = new FixedWindowRateLimiter.Config();
    config.setMaxRequests(maxRequests);
    config.setWindowSeconds(windowSeconds);
    return config;
  }
}

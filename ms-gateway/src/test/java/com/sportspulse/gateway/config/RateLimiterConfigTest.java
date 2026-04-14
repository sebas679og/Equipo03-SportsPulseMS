package com.sportspulse.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportspulse.gateway.config.constants.InternalHeaders;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

@DisplayName("RateLimiterConfig Tests")
class RateLimiterConfigTest {

  private RateLimiterConfig rateLimiterConfig;

  @BeforeEach
  void setUp() {
    rateLimiterConfig = new RateLimiterConfig();
  }

  // ─── ipKeyResolver ───────────────────────────────────────────────────────

  @Test
  @DisplayName("ipKeyResolver resolves key from X-Forwarded-For header (single IP)")
  void ipKeyResolver_withForwardedHeader_returnsSingleIp() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/test")
            .header(InternalHeaders.CLIENT_IP_HEADER, "10.0.0.5")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    KeyResolver resolver = rateLimiterConfig.ipKeyResolver();

    StepVerifier.create(resolver.resolve(exchange)).expectNext("10.0.0.5").verifyComplete();
  }

  @Test
  @DisplayName("ipKeyResolver trims and splits comma-separated X-Forwarded-For header")
  void ipKeyResolver_withCommaForwardedHeader_returnsFirstIp() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/test")
            .header(InternalHeaders.CLIENT_IP_HEADER, "10.0.0.1, 10.0.0.2, 10.0.0.3")
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    KeyResolver resolver = rateLimiterConfig.ipKeyResolver();

    StepVerifier.create(resolver.resolve(exchange)).expectNext("10.0.0.1").verifyComplete();
  }

  @Test
  @DisplayName(
      "ipKeyResolver ignores blank X-Forwarded-For header and falls back to remote address")
  void ipKeyResolver_withBlankForwardedHeader_fallsBackToRemoteAddress() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/test")
            .header(InternalHeaders.CLIENT_IP_HEADER, "   ")
            .remoteAddress(new InetSocketAddress("192.168.1.100", 12345))
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    KeyResolver resolver = rateLimiterConfig.ipKeyResolver();

    StepVerifier.create(resolver.resolve(exchange)).expectNext("192.168.1.100").verifyComplete();
  }

  @Test
  @DisplayName("ipKeyResolver falls back to remote address when header is absent")
  void ipKeyResolver_withoutForwardedHeader_usesRemoteAddress() {
    MockServerHttpRequest request =
        MockServerHttpRequest.get("/api/test")
            .remoteAddress(new InetSocketAddress("172.16.0.1", 8080))
            .build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    KeyResolver resolver = rateLimiterConfig.ipKeyResolver();

    StepVerifier.create(resolver.resolve(exchange)).expectNext("172.16.0.1").verifyComplete();
  }

  @Test
  @DisplayName("ipKeyResolver returns 'unknown' when neither header nor remote address is present")
  void ipKeyResolver_withNoAddressInfo_returnsUnknown() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
    MockServerWebExchange exchange = MockServerWebExchange.from(request);

    KeyResolver resolver = rateLimiterConfig.ipKeyResolver();

    StepVerifier.create(resolver.resolve(exchange)).expectNext("unknown").verifyComplete();
  }

  // ─── defaultRedisRateLimiter ─────────────────────────────────────────────

  @Test
  @DisplayName("defaultRedisRateLimiter bean is not null")
  void defaultRedisRateLimiter_isNotNull() {
    RedisRateLimiter limiter = rateLimiterConfig.defaultRedisRateLimiter();
    assertThat(limiter).isNotNull();
  }

  @Test
  @DisplayName("defaultRedisRateLimiter is a distinct instance from bruteForceRateLimiter")
  void defaultAndBruteForce_areDistinctInstances() {
    RedisRateLimiter defaultLimiter = rateLimiterConfig.defaultRedisRateLimiter();
    RedisRateLimiter bruteForceLimiter = rateLimiterConfig.bruteForceRateLimiter();
    assertThat(defaultLimiter).isNotSameAs(bruteForceLimiter);
  }

  // ─── bruteForceRateLimiter ────────────────────────────────────────────────

  @Test
  @DisplayName("bruteForceRateLimiter bean is not null")
  void bruteForceRateLimiter_isNotNull() {
    RedisRateLimiter limiter = rateLimiterConfig.bruteForceRateLimiter();
    assertThat(limiter).isNotNull();
  }
}

package com.sportspulse.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("GatewayRouteFactory Tests")
class GatewayRouteFactoryTest {

  @Mock private RedisRateLimiter defaultRateLimiter;
  @Mock private RedisRateLimiter bruteForceRateLimiter;
  @Mock private KeyResolver keyResolver;
  @Mock private JsonResponseWriter responseWriter;
  @Mock private GatewayFilterChain chain;

  private GatewayRouteFactory gatewayRouteFactory;

  @BeforeEach
  void setUp() {
    gatewayRouteFactory =
        new GatewayRouteFactory(
            defaultRateLimiter, bruteForceRateLimiter, keyResolver, responseWriter);
  }

  // ─── applyStandardFilters / applyBruteForceFilters ────────────────────────

  @Test
  @DisplayName("applyStandardFilters returns non-null function")
  void applyStandardFilters_returnsNonNullFunction() {
    Function<GatewayFilterSpec, UriSpec> result =
        gatewayRouteFactory.applyStandardFilters("ms-auth");
    assertThat(result).isNotNull();
  }

  @Test
  @DisplayName("applyBruteForceFilters returns non-null function")
  void applyBruteForceFilters_returnsNonNullFunction() {
    Function<GatewayFilterSpec, UriSpec> result =
        gatewayRouteFactory.applyBruteForceFilters("ms-auth");
    assertThat(result).isNotNull();
  }

  // ─── rate-limit filter — standard limiter ────────────────────────────────

  @Test
  @DisplayName("standard filter: allowed request is forwarded to chain")
  void rateLimitFilter_standard_allowedRequest_proceedsToChain() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("127.0.0.1"));
    when(defaultRateLimiter.isAllowed(anyString(), anyString()))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(responseWriter);
  }

  @Test
  @DisplayName("standard filter: denied request writes HTTP 429 and does not reach chain")
  void rateLimitFilter_standard_deniedRequest_returns429() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("127.0.0.1"));
    when(defaultRateLimiter.isAllowed(anyString(), anyString()))
        .thenReturn(Mono.just(deniedResponse()));
    when(responseWriter.write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later"))
        .thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(responseWriter)
        .write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later");
    verifyNoInteractions(chain);
  }

  // ─── rate-limit filter — brute-force limiter ──────────────────────────────

  @Test
  @DisplayName("brute-force filter: allowed request is forwarded to chain")
  void rateLimitFilter_bruteForce_allowedRequest_proceedsToChain() {
    ServerWebExchange exchange = exchangeFor("/api/auth/login");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("10.0.0.1"));
    when(bruteForceRateLimiter.isAllowed(anyString(), anyString()))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyBruteForceFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(responseWriter);
  }

  @Test
  @DisplayName("brute-force filter: denied request writes HTTP 429 and does not reach chain")
  void rateLimitFilter_bruteForce_deniedRequest_returns429() {
    ServerWebExchange exchange = exchangeFor("/api/auth/login");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("10.0.0.1"));
    when(bruteForceRateLimiter.isAllowed(anyString(), anyString()))
        .thenReturn(Mono.just(deniedResponse()));
    when(responseWriter.write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later"))
        .thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyBruteForceFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(responseWriter)
        .write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later");
    verifyNoInteractions(chain);
  }

  // ─── route ID resolution ──────────────────────────────────────────────────

  @Test
  @DisplayName("rate-limit filter falls back to 'default' routeId when no route attribute present")
  void rateLimitFilter_noRouteAttribute_usesDefaultRouteId() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("192.168.1.1"));
    when(defaultRateLimiter.isAllowed(eq("default"), eq("192.168.1.1")))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-test"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(defaultRateLimiter).isAllowed("default", "192.168.1.1");
  }

  @Test
  @DisplayName("rate-limit filter uses route ID from exchange attribute when present")
  void rateLimitFilter_withRouteAttribute_usesRouteId() {
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

    Route route = mock(Route.class);
    when(route.getId()).thenReturn("ms-auth-route");
    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("10.10.10.10"));
    when(defaultRateLimiter.isAllowed(eq("ms-auth-route"), eq("10.10.10.10")))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(defaultRateLimiter).isAllowed("ms-auth-route", "10.10.10.10");
  }

  // ─── helpers ──────────────────────────────────────────────────────────────

  /**
   * Applies the factory function to a deep-stub mock of {@link GatewayFilterSpec}.
   *
   * <p>{@code RETURNS_DEEP_STUBS} makes every fluent method (including {@code circuitBreaker()},
   * whose exact return type varies by Spring Cloud Gateway version) automatically return a mock of
   * the correct type, so the entire {@code f.filter(...).circuitBreaker(...)} chain compiles and
   * runs without any manual {@code thenReturn} wiring.
   *
   * <p>The only method we care about is {@code filter(GatewayFilter)}: we intercept it with {@code
   * doAnswer} to capture the lambda before delegating back to the deep stub.
   */
  private GatewayFilter captureFilter(Function<GatewayFilterSpec, UriSpec> factoryFn) {
    List<GatewayFilter> captured = new ArrayList<>();

    GatewayFilterSpec spec = mock(GatewayFilterSpec.class, RETURNS_DEEP_STUBS);
    doAnswer(
            invocation -> {
              captured.add(invocation.getArgument(0));
              return spec; // return the same mock to keep the fluent chain alive
            })
        .when(spec)
        .filter(any(GatewayFilter.class));

    factoryFn.apply(spec);

    assertThat(captured)
        .as("Expected GatewayRouteFactory to register at least one GatewayFilter")
        .isNotEmpty();
    return captured.get(0);
  }

  private ServerWebExchange exchangeFor(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
  }

  /**
   * {@link RedisRateLimiter.Response#isAllowed()} is declared final in {@link
   * org.springframework.cloud.gateway.filter.ratelimit.RateLimiter.Response}, so it cannot be
   * mocked. We use the real public constructor instead.
   */
  private RedisRateLimiter.Response allowedResponse() {
    return new RedisRateLimiter.Response(true, Collections.emptyMap());
  }

  private RedisRateLimiter.Response deniedResponse() {
    return new RedisRateLimiter.Response(false, Collections.emptyMap());
  }
}

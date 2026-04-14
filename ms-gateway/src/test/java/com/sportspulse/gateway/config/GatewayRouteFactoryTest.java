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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
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

  @Mock private FixedWindowRateLimiter fixedWindowRateLimiter;
  @Mock private KeyResolver keyResolver;
  @Mock private JsonResponseWriter responseWriter;
  @Mock private GatewayFilterChain chain;

  private GatewayRouteFactory gatewayRouteFactory;

  @BeforeEach
  void setUp() {
    gatewayRouteFactory =
        new GatewayRouteFactory(fixedWindowRateLimiter, keyResolver, responseWriter);
  }

  // ─── applyStandardFilters / applyBruteForceFilters ────────────────────────

  @Test
  @DisplayName("applyStandardFilters returns non-null function")
  void applyStandardFilters_returnsNonNullFunction() {
    assertThat(gatewayRouteFactory.applyStandardFilters("ms-auth")).isNotNull();
  }

  @Test
  @DisplayName("applyBruteForceFilters returns non-null function")
  void applyBruteForceFilters_returnsNonNullFunction() {
    assertThat(gatewayRouteFactory.applyBruteForceFilters("ms-auth")).isNotNull();
  }

  // ─── rate-limit filter — standard limiter ────────────────────────────────

  @Test
  @DisplayName("standard filter: allowed request is forwarded to chain")
  void rateLimitFilter_standard_allowedRequest_proceedsToChain() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("127.0.0.1"));
    when(fixedWindowRateLimiter.isAllowed(
            anyString(), anyString(), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(responseWriter);
  }

  @Test
  @DisplayName("standard filter: denied request without retryAfter writes generic 429")
  void rateLimitFilter_standard_deniedRequest_returns429_withoutRetryAfter() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("127.0.0.1"));
    when(fixedWindowRateLimiter.isAllowed(
            anyString(), anyString(), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(deniedResponse(null)));
    when(responseWriter.write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later"))
        .thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(responseWriter)
        .write(exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again later");
    verifyNoInteractions(chain);
  }

  @Test
  @DisplayName("standard filter: denied request with retryAfter writes 429 with time")
  void rateLimitFilter_standard_deniedRequest_returns429_withRetryAfter() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("127.0.0.1"));
    when(fixedWindowRateLimiter.isAllowed(
            anyString(), anyString(), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(deniedResponse("42s")));
    when(responseWriter.write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again in 42s"))
        .thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(responseWriter)
        .write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again in 42s");
    verifyNoInteractions(chain);
  }

  // ─── rate-limit filter — brute-force limiter ──────────────────────────────

  @Test
  @DisplayName("brute-force filter: allowed request is forwarded to chain")
  void rateLimitFilter_bruteForce_allowedRequest_proceedsToChain() {
    ServerWebExchange exchange = exchangeFor("/api/auth/login");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("10.0.0.1"));
    when(fixedWindowRateLimiter.isAllowed(
            anyString(), anyString(), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyBruteForceFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(responseWriter);
  }

  @Test
  @DisplayName("brute-force filter: denied request writes 429 with retryAfter")
  void rateLimitFilter_bruteForce_deniedRequest_returns429_withRetryAfter() {
    ServerWebExchange exchange = exchangeFor("/api/auth/login");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("10.0.0.1"));
    when(fixedWindowRateLimiter.isAllowed(
            anyString(), anyString(), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(deniedResponse("30s")));
    when(responseWriter.write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again in 30s"))
        .thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyBruteForceFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(responseWriter)
        .write(
            exchange, HttpStatus.TOO_MANY_REQUESTS, "Too many requests, please try again in 30s");
    verifyNoInteractions(chain);
  }

  // ─── route ID resolution ──────────────────────────────────────────────────

  @Test
  @DisplayName("rate-limit filter falls back to 'default' routeId when no route attribute present")
  void rateLimitFilter_noRouteAttribute_usesDefaultRouteId() {
    ServerWebExchange exchange = exchangeFor("/api/test");

    when(keyResolver.resolve(exchange)).thenReturn(Mono.just("192.168.1.1"));
    when(fixedWindowRateLimiter.isAllowed(
            eq("default"), eq("192.168.1.1"), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-test"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(fixedWindowRateLimiter)
        .isAllowed(eq("default"), eq("192.168.1.1"), any(FixedWindowRateLimiter.Config.class));
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
    when(fixedWindowRateLimiter.isAllowed(
            eq("ms-auth-route"), eq("10.10.10.10"), any(FixedWindowRateLimiter.Config.class)))
        .thenReturn(Mono.just(allowedResponse()));
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    GatewayFilter filter = captureFilter(gatewayRouteFactory.applyStandardFilters("ms-auth"));

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(fixedWindowRateLimiter)
        .isAllowed(
            eq("ms-auth-route"), eq("10.10.10.10"), any(FixedWindowRateLimiter.Config.class));
  }

  // ─── helpers ──────────────────────────────────────────────────────────────

  private GatewayFilter captureFilter(Function<GatewayFilterSpec, UriSpec> factoryFn) {
    List<GatewayFilter> captured = new ArrayList<>();

    GatewayFilterSpec spec = mock(GatewayFilterSpec.class, RETURNS_DEEP_STUBS);
    doAnswer(
            invocation -> {
              captured.add(invocation.getArgument(0));
              return spec;
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

  private RateLimiter.Response allowedResponse() {
    return new RateLimiter.Response(true, Collections.emptyMap());
  }

  private RateLimiter.Response deniedResponse(String retryAfter) {
    Map<String, String> headers = new HashMap<>();
    if (retryAfter != null) {
      headers.put("X-RateLimit-Reset-In", retryAfter);
    }
    return new RateLimiter.Response(false, headers);
  }
}

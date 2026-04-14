package com.sportspulse.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalRouteBlockerFilter Tests")
class InternalRouteBlockerFilterTest {

  @Mock private JsonResponseWriter jsonResponseWriter;
  @Mock private GatewayFilterChain chain;

  private InternalRouteBlockerFilter filter;

  @BeforeEach
  void setUp() {
    filter = new InternalRouteBlockerFilter(jsonResponseWriter);
  }

  // ─── Blocked paths ────────────────────────────────────────────────────────

  @Test
  @DisplayName("filter blocks request to VALIDATE_TOKEN path with HTTP 404")
  void filter_validateTokenPath_returns404() {
    String blockedPath = ApiPathsServices.Auth.VALIDATE_TOKEN;
    ServerWebExchange exchange = exchangeFor(blockedPath);

    when(jsonResponseWriter.write(eq(exchange), eq(HttpStatus.NOT_FOUND), any(String.class)))
        .thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(jsonResponseWriter)
        .write(eq(exchange), eq(HttpStatus.NOT_FOUND), contains("Route not found"));
    verifyNoInteractions(chain);
  }

  @Test
  @DisplayName("filter error message includes the blocked path")
  void filter_blockedPath_errorMessageContainsPath() {
    String blockedPath = ApiPathsServices.Auth.VALIDATE_TOKEN;
    ServerWebExchange exchange = exchangeFor(blockedPath);

    var messageCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
    when(jsonResponseWriter.write(eq(exchange), eq(HttpStatus.NOT_FOUND), messageCaptor.capture()))
        .thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    assertThat(messageCaptor.getValue()).contains(blockedPath);
  }

  @Test
  @DisplayName("filter blocks path that starts with VALIDATE_TOKEN prefix")
  void filter_pathStartingWithValidateToken_isBlocked() {
    String subPath = ApiPathsServices.Auth.VALIDATE_TOKEN + "/extra";
    ServerWebExchange exchange = exchangeFor(subPath);

    when(jsonResponseWriter.write(eq(exchange), eq(HttpStatus.NOT_FOUND), any()))
        .thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(jsonResponseWriter).write(any(), eq(HttpStatus.NOT_FOUND), any());
    verifyNoInteractions(chain);
  }

  // ─── Allowed paths ────────────────────────────────────────────────────────

  @Test
  @DisplayName("filter allows request to login path and delegates to chain")
  void filter_loginPath_delegatesToChain() {
    ServerWebExchange exchange = exchangeFor("/api/auth/login");
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(jsonResponseWriter);
  }

  @Test
  @DisplayName("filter allows request to arbitrary public path and delegates to chain")
  void filter_publicPath_delegatesToChain() {
    ServerWebExchange exchange = exchangeFor("/api/leagues/standings");
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(jsonResponseWriter);
  }

  @Test
  @DisplayName("filter allows root path and delegates to chain")
  void filter_rootPath_delegatesToChain() {
    ServerWebExchange exchange = exchangeFor("/");
    when(chain.filter(exchange)).thenReturn(Mono.empty());

    StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

    verify(chain).filter(exchange);
    verifyNoInteractions(jsonResponseWriter);
  }

  // ─── Ordered ─────────────────────────────────────────────────────────────

  @Test
  @DisplayName("filter order is HIGHEST_PRECEDENCE")
  void filter_orderIsHighestPrecedence() {
    assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
  }

  // ─── helpers ──────────────────────────────────────────────────────────────

  private ServerWebExchange exchangeFor(String path) {
    return MockServerWebExchange.from(MockServerHttpRequest.get(path).build());
  }

  private static String contains(String substring) {
    return argThat(s -> s != null && s.contains(substring));
  }
}

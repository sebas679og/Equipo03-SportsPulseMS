package com.sportspulse.gateway.config;

import com.sportspulse.gateway.config.constants.InternalHeaders;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

@DisplayName("KeyResolverConfig Tests")
class KeyResolverConfigTest {

  private KeyResolver keyResolver;

  @BeforeEach
  void setUp() {
    keyResolver = new KeyResolverConfig().ipKeyResolver();
  }

  // ─── X-Forwarded-For header present ──────────────────────────────────────

  @Test
  @DisplayName("resolves key from CLIENT_IP_HEADER when present")
  void resolve_clientIpHeader_returnsSingleIp() {
    ServerWebExchange exchange =
        exchangeWithHeader(InternalHeaders.CLIENT_IP_HEADER, "192.168.1.1");

    StepVerifier.create(keyResolver.resolve(exchange)).expectNext("192.168.1.1").verifyComplete();
  }

  @Test
  @DisplayName("resolves first IP when CLIENT_IP_HEADER contains comma-separated list")
  void resolve_clientIpHeader_multipleIps_returnsFirst() {
    ServerWebExchange exchange =
        exchangeWithHeader(InternalHeaders.CLIENT_IP_HEADER, "10.0.0.1, 10.0.0.2, 10.0.0.3");

    StepVerifier.create(keyResolver.resolve(exchange)).expectNext("10.0.0.1").verifyComplete();
  }

  @Test
  @DisplayName("trims whitespace from first IP in CLIENT_IP_HEADER")
  void resolve_clientIpHeader_trimsWhitespace() {
    ServerWebExchange exchange =
        exchangeWithHeader(InternalHeaders.CLIENT_IP_HEADER, "  172.16.0.5  , 172.16.0.6");

    StepVerifier.create(keyResolver.resolve(exchange)).expectNext("172.16.0.5").verifyComplete();
  }

  // ─── CLIENT_IP_HEADER blank or absent ────────────────────────────────────

  @Test
  @DisplayName("falls back to remote address when CLIENT_IP_HEADER is absent")
  void resolve_noHeader_usesRemoteAddress() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/test")
                .remoteAddress(new InetSocketAddress("203.0.113.5", 8080))
                .build());

    StepVerifier.create(keyResolver.resolve(exchange)).expectNext("203.0.113.5").verifyComplete();
  }

  @Test
  @DisplayName("falls back to remote address when CLIENT_IP_HEADER is blank")
  void resolve_blankHeader_usesRemoteAddress() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/test")
                .header(InternalHeaders.CLIENT_IP_HEADER, "   ")
                .remoteAddress(new InetSocketAddress("203.0.113.10", 8080))
                .build());

    StepVerifier.create(keyResolver.resolve(exchange)).expectNext("203.0.113.10").verifyComplete();
  }

  @Test
  @DisplayName("returns 'unknown' when CLIENT_IP_HEADER is absent and no remote address available")
  void resolve_noHeaderNoRemoteAddress_returnsUnknown() {
    ServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.get("/api/test").build());

    StepVerifier.create(keyResolver.resolve(exchange)).expectNext("unknown").verifyComplete();
  }

  // ─── helpers ─────────────────────────────────────────────────────────────

  private ServerWebExchange exchangeWithHeader(String headerName, String headerValue) {
    return MockServerWebExchange.from(
        MockServerHttpRequest.get("/api/test").header(headerName, headerValue).build());
  }
}

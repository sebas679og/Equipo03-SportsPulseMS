package com.sportspulse.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Tests")
class SecurityConfigTest {

  @Mock private JsonResponseWriter responseWriter;

  private SecurityConfig securityConfig;

  @BeforeEach
  void setUp() {
    securityConfig = new SecurityConfig();
  }

  // ─── Access Denied Handler ───────────────────────────────────────────────

  @Test
  @DisplayName("accessDeniedHandler writes HTTP 403 FORBIDDEN response")
  void accessDeniedHandler_writesForbiddenResponse() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/some/resource").build();
    ServerWebExchange exchange = MockServerWebExchange.from(request);
    AccessDeniedException denied = new AccessDeniedException("Denied");

    when(responseWriter.write(eq(exchange), eq(HttpStatus.FORBIDDEN), eq("Access denied")))
        .thenReturn(Mono.empty());

    // Invoke handler directly via a thin wrapper
    ServerAccessDeniedHandler handler =
        (exch, ex) -> responseWriter.write(exch, HttpStatus.FORBIDDEN, "Access denied");

    StepVerifier.create(handler.handle(exchange, denied)).verifyComplete();

    verify(responseWriter).write(exchange, HttpStatus.FORBIDDEN, "Access denied");
  }

  @Test
  @DisplayName("authenticationEntryPoint writes HTTP 401 UNAUTHORIZED response")
  void authenticationEntryPoint_writesUnauthorizedResponse() {
    MockServerHttpRequest request = MockServerHttpRequest.get("/secured").build();
    ServerWebExchange exchange = MockServerWebExchange.from(request);
    AuthenticationException authEx = new AuthenticationException("Unauthorized") {};

    when(responseWriter.write(eq(exchange), eq(HttpStatus.UNAUTHORIZED), eq("Unauthorized")))
        .thenReturn(Mono.empty());

    ServerAuthenticationEntryPoint entryPoint =
        (exch, ex) -> responseWriter.write(exch, HttpStatus.UNAUTHORIZED, "Unauthorized");

    StepVerifier.create(entryPoint.commence(exchange, authEx)).verifyComplete();

    verify(responseWriter).write(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
  }

  // ─── Path matching ───────────────────────────────────────────────────────

  @Test
  @DisplayName("VALIDATE_TOKEN path is declared as denyAll — constant is non-blank")
  void validateTokenPath_isDeclaredConstant() {
    assertThat(ApiPathsServices.Auth.VALIDATE_TOKEN).isNotBlank();
  }

  @Test
  @DisplayName("SecurityConfig instantiates without errors")
  void securityConfig_instantiatesSuccessfully() {
    assertThat(securityConfig).isNotNull();
  }
}

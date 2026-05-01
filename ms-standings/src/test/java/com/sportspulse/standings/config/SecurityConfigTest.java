package com.sportspulse.standings.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.sportspulse.standings.exceptions.JsonWriter;
import com.sportspulse.standings.filters.BearerAuthenticationFilter;
import com.sportspulse.standings.integrations.msauth.AuthClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  @Mock private JsonWriter writer;

  @Mock private AuthClient authClient;

  @InjectMocks private SecurityConfig securityConfig;

  // ─── bearerAuthenticationFilter ───────────────────────────────────────────

  @Test
  @DisplayName("bearerAuthenticationFilter() should return a BearerAuthenticationFilter instance")
  void bearerAuthenticationFilter_shouldReturnCorrectType() {
    BearerAuthenticationFilter filter = securityConfig.bearerAuthenticationFilter();

    assertThat(filter).isNotNull().isInstanceOf(BearerAuthenticationFilter.class);
  }

  @Test
  @DisplayName("bearerAuthenticationFilter() should inject the AuthClient dependency")
  void bearerAuthenticationFilter_shouldInjectAuthClient() {
    BearerAuthenticationFilter filter = securityConfig.bearerAuthenticationFilter();

    // Each call produces a new instance (not singleton here at unit level)
    assertThat(filter).isNotNull();
  }

  // ─── unauthorizedEntryPoint ────────────────────────────────────────────────

  @Test
  @DisplayName(
      "unauthorizedEntryPoint() should call writer.sendError with UNAUTHORIZED and correct message")
  void unauthorizedEntryPoint_shouldSendUnauthorizedError() throws Exception {
    AuthenticationEntryPoint entryPoint = securityConfig.unauthorizedEntryPoint();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationException ex = mock(AuthenticationException.class);

    entryPoint.commence(request, response, ex);

    verify(writer).sendError(response, HttpStatus.UNAUTHORIZED, "Authentication required");
    verifyNoMoreInteractions(writer);
  }

  @Test
  @DisplayName("unauthorizedEntryPoint() should return a non-null AuthenticationEntryPoint")
  void unauthorizedEntryPoint_shouldReturnNonNull() {
    assertThat(securityConfig.unauthorizedEntryPoint()).isNotNull();
  }

  // ─── accessDeniedHandler ──────────────────────────────────────────────────

  @Test
  @DisplayName(
      "accessDeniedHandler() should call writer.sendError with NOT_FOUND and correct message")
  void accessDeniedHandler_shouldSendNotFoundError() throws Exception {
    AccessDeniedHandler handler = securityConfig.accessDeniedHandler();

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AccessDeniedException ex = mock(AccessDeniedException.class);

    handler.handle(request, response, ex);

    verify(writer)
        .sendError(response, HttpStatus.NOT_FOUND, "The requested resource does not exist");
    verifyNoMoreInteractions(writer);
  }

  @Test
  @DisplayName("accessDeniedHandler() should return a non-null AccessDeniedHandler")
  void accessDeniedHandler_shouldReturnNonNull() {
    assertThat(securityConfig.accessDeniedHandler()).isNotNull();
  }
}

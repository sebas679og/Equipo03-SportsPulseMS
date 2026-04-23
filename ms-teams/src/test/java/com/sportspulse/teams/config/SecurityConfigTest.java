package com.sportspulse.teams.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sportspulse.teams.config.constants.InternalHeaders;
import com.sportspulse.teams.exceptions.JsonWriter;
import com.sportspulse.teams.filters.BearerAuthenticationFilter;
import com.sportspulse.teams.filters.InternalApiKeyAuthenticationFilter;
import jakarta.servlet.FilterChain;
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
import org.springframework.web.filter.OncePerRequestFilter;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  @Mock private JsonWriter writer;

  @InjectMocks private SecurityConfig securityConfig;

  // -------------------------------------------------------------------------
  // Bean creation
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("bearerAuthenticationFilter() returns a BearerAuthenticationFilter instance")
  void bearerAuthenticationFilter_returnsBearerAuthenticationFilter() {
    OncePerRequestFilter filter = securityConfig.bearerAuthenticationFilter();

    assertThat(filter).isInstanceOf(BearerAuthenticationFilter.class);
  }

  @Test
  @DisplayName(
      "internalApiKeyAuthenticationFilter() returns an InternalApiKeyAuthenticationFilter instance")
  void internalApiKeyAuthenticationFilter_returnsInternalApiKeyAuthenticationFilter() {
    OncePerRequestFilter filter = securityConfig.internalApiKeyAuthenticationFilter();

    assertThat(filter).isInstanceOf(InternalApiKeyAuthenticationFilter.class);
  }

  @Test
  @DisplayName("dualHeaderGuardFilter() returns a non-null OncePerRequestFilter instance")
  void dualHeaderGuardFilter_returnsOncePerRequestFilter() {
    OncePerRequestFilter filter = securityConfig.dualHeaderGuardFilter();

    assertThat(filter).isNotNull();
  }

  @Test
  @DisplayName("unauthorizedEntryPoint() returns a non-null AuthenticationEntryPoint")
  void unauthorizedEntryPoint_returnsNonNullEntryPoint() {
    AuthenticationEntryPoint entryPoint = securityConfig.unauthorizedEntryPoint();

    assertThat(entryPoint).isNotNull();
  }

  @Test
  @DisplayName("accessDeniedHandler() returns a non-null AccessDeniedHandler")
  void accessDeniedHandler_returnsNonNullHandler() {
    AccessDeniedHandler handler = securityConfig.accessDeniedHandler();

    assertThat(handler).isNotNull();
  }

  // -------------------------------------------------------------------------
  // dualHeaderGuardFilter() — both headers present → 401
  // -------------------------------------------------------------------------

  @Test
  @DisplayName(
      "dualHeaderGuardFilter() sends UNAUTHORIZED when both Bearer and API key headers are present")
  void dualHeaderGuardFilter_whenBothHeadersPresent_sendsUnauthorized() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    given(request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER)).willReturn("Bearer token");
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("api-key");

    securityConfig.dualHeaderGuardFilter().doFilter(request, response, filterChain);

    then(writer)
        .should()
        .sendError(response, HttpStatus.UNAUTHORIZED, "Authentication could not be obtained");
  }

  @Test
  @DisplayName("dualHeaderGuardFilter() does not continue chain when both headers are present")
  void dualHeaderGuardFilter_whenBothHeadersPresent_doesNotContinueChain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    given(request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER)).willReturn("Bearer token");
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("api-key");

    securityConfig.dualHeaderGuardFilter().doFilter(request, response, filterChain);

    then(filterChain).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // dualHeaderGuardFilter() — only Bearer header → chain continues
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("dualHeaderGuardFilter() continues chain when only Bearer header is present")
  void dualHeaderGuardFilter_whenOnlyBearerHeader_continuesChain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    given(request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER)).willReturn("Bearer token");
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(null);

    securityConfig.dualHeaderGuardFilter().doFilter(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
  }

  @Test
  @DisplayName(
      "dualHeaderGuardFilter() does not call JsonWriter when only Bearer header is present")
  void dualHeaderGuardFilter_whenOnlyBearerHeader_doesNotCallJsonWriter() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    given(request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER)).willReturn("Bearer token");
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(null);

    securityConfig.dualHeaderGuardFilter().doFilter(request, response, filterChain);

    then(writer).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // dualHeaderGuardFilter() — only API key header → chain continues
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("dualHeaderGuardFilter() continues chain when only API key header is present")
  void dualHeaderGuardFilter_whenOnlyApiKeyHeader_continuesChain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    given(request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER)).willReturn(null);
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("api-key");

    securityConfig.dualHeaderGuardFilter().doFilter(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
  }

  // -------------------------------------------------------------------------
  // dualHeaderGuardFilter() — no headers → chain continues
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("dualHeaderGuardFilter() continues chain when neither header is present")
  void dualHeaderGuardFilter_whenNoHeaders_continuesChain() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    given(request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER)).willReturn(null);
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(null);

    securityConfig.dualHeaderGuardFilter().doFilter(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
  }

  // -------------------------------------------------------------------------
  // unauthorizedEntryPoint() — sends 401
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("unauthorizedEntryPoint() sends UNAUTHORIZED with correct message")
  void unauthorizedEntryPoint_sendsUnauthorizedWithCorrectMessage() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AuthenticationException authException = mock(AuthenticationException.class);

    securityConfig.unauthorizedEntryPoint().commence(request, response, authException);

    then(writer).should().sendError(response, HttpStatus.UNAUTHORIZED, "Authentication required");
  }

  // -------------------------------------------------------------------------
  // accessDeniedHandler() — sends 404
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("accessDeniedHandler() sends NOT_FOUND with correct message")
  void accessDeniedHandler_sendsNotFoundWithCorrectMessage() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    AccessDeniedException accessDeniedException = mock(AccessDeniedException.class);

    securityConfig.accessDeniedHandler().handle(request, response, accessDeniedException);

    then(writer)
        .should()
        .sendError(response, HttpStatus.NOT_FOUND, "The requested resource does not exist");
  }
}

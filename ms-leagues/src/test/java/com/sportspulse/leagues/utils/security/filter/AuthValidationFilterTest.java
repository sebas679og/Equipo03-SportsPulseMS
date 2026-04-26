package com.sportspulse.leagues.utils.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportspulse.leagues.exceptions.CustomServiceUnavailableException;
import com.sportspulse.leagues.integration.msauth.AuthClient;
import com.sportspulse.leagues.integration.msauth.dto.UserResponse;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthValidationFilter Tests")
class AuthValidationFilterTest {

  @Mock private AuthClient authClient;
  @Mock private FilterChain filterChain;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ObjectMapper testObjectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }

  @Test
  @DisplayName("shouldNotFilter should skip docs path")
  void shouldNotFilter_shouldSkipSwaggerPath() {
    AuthValidationFilter filter = new AuthValidationFilter(authClient, testObjectMapper());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/swagger-ui/index.html");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  @DisplayName("doFilterInternal should return 401 when token is missing")
  void doFilterInternal_shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
    AuthValidationFilter filter = new AuthValidationFilter(authClient, testObjectMapper());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/leagues");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal should authenticate and continue chain for valid token")
  void doFilterInternal_shouldAuthenticateWhenTokenIsValid() throws Exception {
    AuthValidationFilter filter = new AuthValidationFilter(authClient, testObjectMapper());

    when(authClient.isTokenValid(anyString()))
        .thenReturn(
        new UserResponse(true, null, "javier", "USER"));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/leagues");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal should return 503 when auth service is unavailable")
  void doFilterInternal_shouldReturnServiceUnavailableWhenAuthFails() throws Exception {
    AuthValidationFilter filter = new AuthValidationFilter(authClient, testObjectMapper());

    when(authClient.isTokenValid(anyString()))
      .thenThrow(new CustomServiceUnavailableException("down"));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/leagues");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(503);
    verify(filterChain, never()).doFilter(request, response);
  }
}
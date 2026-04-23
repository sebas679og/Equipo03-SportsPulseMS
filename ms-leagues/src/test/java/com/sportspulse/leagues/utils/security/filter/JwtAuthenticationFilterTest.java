package com.sportspulse.leagues.utils.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportspulse.leagues.config.properties.JwtProperties;
import com.sportspulse.leagues.services.components.JwtTokenService;
import io.jsonwebtoken.Claims;
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
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenService jwtTokenService;
  @Mock private JwtProperties jwtProperties;
  @Mock private FilterChain filterChain;
  @Mock private Claims claims;

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
    JwtAuthenticationFilter filter =
        new JwtAuthenticationFilter(jwtTokenService, jwtProperties, testObjectMapper());

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/swagger-ui/index.html");

    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  @DisplayName("doFilterInternal should return 401 when token is missing")
  void doFilterInternal_shouldReturnUnauthorizedWhenTokenMissing() throws Exception {
    JwtAuthenticationFilter filter =
        new JwtAuthenticationFilter(jwtTokenService, jwtProperties, testObjectMapper());

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
    final JwtAuthenticationFilter filter =
        new JwtAuthenticationFilter(jwtTokenService, jwtProperties, testObjectMapper());

    when(jwtProperties.getTokenType()).thenReturn("Bearer");
    when(jwtTokenService.validateAndExtract("valid-token")).thenReturn(claims);
    when(claims.get("username", String.class)).thenReturn("javier");
    when(claims.get("role", String.class)).thenReturn("USER");

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/leagues");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    verify(filterChain).doFilter(request, response);
  }
}

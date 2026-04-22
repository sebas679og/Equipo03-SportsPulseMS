package com.sportspulse.teams.security.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sportspulse.teams.client.AuthClient;
import com.sportspulse.teams.constants.Errors;
import com.sportspulse.teams.constants.HttpHeaders;
import com.sportspulse.teams.dto.internal.ValidateResponse;
import com.sportspulse.teams.security.writer.ErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private AuthClient authClient;

  @Mock private ErrorWriter errorWriter;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private JwtAuthenticationFilter filter;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_shouldReturn401_whenAuthHeaderIsMissing() throws Exception {

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(errorWriter)
        .write(
            response,
            HttpStatus.UNAUTHORIZED,
            Errors.Code.MISSING_TOKEN,
            Errors.Message.MISSING_TOKEN);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void doFilterInternal_shouldReturn401_whenAuthHeaderHasNoBearerPrefix() throws Exception {

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("InvalidToken");

    filter.doFilterInternal(request, response, filterChain);

    verify(errorWriter)
        .write(
            response,
            HttpStatus.UNAUTHORIZED,
            Errors.Code.MISSING_TOKEN,
            Errors.Message.MISSING_TOKEN);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void doFilterInternal_shouldReturn401_whenTokenIsInvalid() throws Exception {

    String token = "Bearer invalid.token";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
    when(authClient.validate(token)).thenReturn(new ValidateResponse(false, null, null, null));

    filter.doFilterInternal(request, response, filterChain);

    verify(errorWriter)
        .write(
            response,
            HttpStatus.UNAUTHORIZED,
            Errors.Code.INVALID_TOKEN,
            Errors.Message.INVALID_TOKEN);
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  void doFilterInternal_shouldContinueChain_whenTokenIsValid() throws Exception {

    String token = "Bearer valid.token";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
    when(authClient.validate(token))
        .thenReturn(new ValidateResponse(true, "uuid-123", "javier_ruiz15", "USER"));

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
        .isEqualTo("javier_ruiz15");
  }

  @Test
  void doFilterInternal_shouldSetCorrectAuthorities_whenTokenIsValid() throws Exception {

    String token = "Bearer valid.token";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
    when(authClient.validate(token))
        .thenReturn(new ValidateResponse(true, "uuid-123", "javier_ruiz15", "USER"));

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_USER");
  }

  @Test
  void doFilterInternal_shouldReturn401_whenFeignThrowsException() throws Exception {

    String token = "Bearer valid.token";
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
    when(authClient.validate(token)).thenThrow(new RuntimeException("Connection refused"));

    filter.doFilterInternal(request, response, filterChain);

    verify(errorWriter)
        .write(
            response,
            HttpStatus.UNAUTHORIZED,
            Errors.Code.TOKEN_VALIDATION_ERROR,
            Errors.Message.TOKEN_VALIDATION_ERROR);
    verify(filterChain, never()).doFilter(any(), any());
  }
}

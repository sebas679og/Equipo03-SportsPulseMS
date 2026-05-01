package com.sportspulse.fixtures.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.fixtures.config.properties.AuthProperties;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.security.writer.ErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class InternalKeyAuthenticationFilterTest {

  @Mock private AuthProperties authProperties;

  @Mock private ErrorWriter writer;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private InternalKeyAuthenticationFilter internalFilter;

  private final String validKey = "Secret-Api-key1_";

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_ShouldSetAuthentication_WhenKeyIsValid()
      throws ServletException, IOException {

    when(request.getHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY)).thenReturn(validKey);
    when(authProperties.getApiKey()).thenReturn(validKey);

    internalFilter.doFilterInternal(request, response, filterChain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(
        "internal-service", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    assertTrue(
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(HttpHeaders.Auth.ROLE_AUTH_INTERNAL)));

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(writer);
  }

  @Test
  void doFilterInternal_ShouldSkip_WhenNoHeaderPresent() throws ServletException, IOException {

    when(request.getHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY)).thenReturn(null);

    internalFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(writer);
  }

  @Test
  void doFilterInternal_ShouldStopAndSendError_WhenKeyIsInvalid()
      throws ServletException, IOException {

    String invalidKey = "wrong-key";
    when(request.getHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY)).thenReturn(invalidKey);
    when(authProperties.getApiKey()).thenReturn(validKey);

    internalFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());

    verify(filterChain, never()).doFilter(request, response);

    verify(writer).sendError(eq(response), eq(HttpStatus.UNAUTHORIZED), anyString());
  }

  @Test
  void getClientIp_ShouldUseForwardedHeader_WhenPresent() throws ServletException, IOException {

    when(request.getHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY)).thenReturn("any");
    when(authProperties.getApiKey()).thenReturn("other");
    when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");

    internalFilter.doFilterInternal(request, response, filterChain);

    verify(writer).sendError(eq(response), eq(HttpStatus.UNAUTHORIZED), anyString());
  }
}

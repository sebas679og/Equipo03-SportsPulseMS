package com.sportspulse.fixtures.security.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.fixtures.client.AuthClient;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.dto.internal.InternalUserResponse;
import com.sportspulse.fixtures.exceptions.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

  @Mock private AuthClient authClient;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private AuthenticationFilter authenticationFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_ShouldSetAuthentication_WhenTokenIsValid()
      throws ServletException, IOException {

    String token = "valid-token";
    String authHeader = HttpHeaders.Auth.BEARER + token;
    InternalUserResponse mockUser =
        new InternalUserResponse(UUID.randomUUID(), "testUser", "ADMIN", true);

    when(request.getHeader(HttpHeaders.Auth.AUTHORIZATION)).thenReturn(authHeader);
    when(authClient.isTokenValid(token)).thenReturn(mockUser);

    authenticationFilter.doFilterInternal(request, response, filterChain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(mockUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_ShouldSkip_WhenNoAuthHeader() throws ServletException, IOException {

    when(request.getHeader(HttpHeaders.Auth.AUTHORIZATION)).thenReturn(null);

    authenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(authClient);
  }

  @Test
  void doFilterInternal_ShouldClearContext_WhenTokenIsInvalid()
      throws ServletException, IOException {

    String token = "invalid-token";
    String authHeader = HttpHeaders.Auth.BEARER + token;

    when(request.getHeader(HttpHeaders.Auth.AUTHORIZATION)).thenReturn(authHeader);
    when(authClient.isTokenValid(token)).thenThrow(new UnauthorizedException("Invalid token"));

    authenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_ShouldHandleMalFormatedHeader() throws ServletException, IOException {

    when(request.getHeader(HttpHeaders.Auth.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

    authenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(authClient);
  }
}

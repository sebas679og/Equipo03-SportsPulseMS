package com.sportspulse.standings.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sportspulse.standings.exceptions.CustomBadGatewayException;
import com.sportspulse.standings.exceptions.CustomUnauthorizedException;
import com.sportspulse.standings.integrations.msauth.AuthClient;
import com.sportspulse.standings.integrations.msauth.dto.UserResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class BearerAuthenticationFilterTest {

  @Mock private AuthClient authClient;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private BearerAuthenticationFilter filter;

  @BeforeEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void cleanUpSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  // ─── No Authorization header ──────────────────────────────────────────────

  @Test
  @DisplayName("Should skip authentication and continue chain when Authorization header is absent")
  void doFilterInternal_whenNoAuthorizationHeader_shouldContinueChain() throws Exception {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(authClient);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  // ─── Non-Bearer Authorization header ─────────────────────────────────────

  @Test
  @DisplayName(
      "Should skip authentication and continue chain when Authorization header is not Bearer")
  void doFilterInternal_whenAuthorizationHeaderIsNotBearer_shouldContinueChain() throws Exception {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic dXNlcjpwYXNz");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(authClient);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName(
      "Should skip authentication and continue "
          + "chain when Authorization header is 'Bearer ' with no token")
  void doFilterInternal_whenBearerPrefixOnlyWithNoToken() throws Exception {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer ");
    when(authClient.isTokenValid(""))
        .thenReturn(new UserResponse(true, UUID.randomUUID(), "test-user", "USER"));

    filter.doFilterInternal(request, response, filterChain);

    verify(authClient).isTokenValid("");
    verify(filterChain).doFilter(request, response);
  }

  // ─── Valid Bearer token ───────────────────────────────────────────────────

  @Test
  @DisplayName("Should set authentication in SecurityContext when token is valid")
  void doFilterInternal_whenTokenIsValid_shouldSetAuthenticationInSecurityContext()
      throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(authClient.isTokenValid("valid-token")).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull().isInstanceOf(UsernamePasswordAuthenticationToken.class);
    assertThat(authentication.getPrincipal()).isEqualTo(user);
  }

  @Test
  @DisplayName("Should set credentials to null when token is valid")
  void doFilterInternal_whenTokenIsValid_shouldSetNullCredentials() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(authClient.isTokenValid("valid-token")).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getCredentials()).isNull();
  }

  @Test
  @DisplayName("Should grant ROLE_ prefixed authority based on user role when token is valid")
  void doFilterInternal_whenTokenIsValid_shouldGrantRoleAuthority() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "ADMIN");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(authClient.isTokenValid("valid-token")).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_ADMIN");
  }

  @Test
  @DisplayName("Should grant AUTH_JWT authority when token is valid")
  void doFilterInternal_whenTokenIsValid_shouldGrantAuthJwtAuthority() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(authClient.isTokenValid("valid-token")).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .contains("AUTH_JWT");
  }

  @Test
  @DisplayName("Should grant exactly two authorities when token is valid")
  void doFilterInternal_whenTokenIsValid_shouldGrantExactlyTwoAuthorities() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(authClient.isTokenValid("valid-token")).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getAuthorities()).hasSize(2);
  }

  @Test
  @DisplayName("Should continue filter chain after setting authentication when token is valid")
  void doFilterInternal_whenTokenIsValid_shouldContinueChain() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer valid-token");
    when(authClient.isTokenValid("valid-token")).thenReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  // ─── CustomUnauthorizedException ─────────────────────────────────────────

  @Test
  @DisplayName(
      "Should clear SecurityContext and continue chain when CustomUnauthorizedException is thrown")
  void doFilterInternal_whenCustomUnauthorizedException_shouldClearContextAndContinueChain()
      throws Exception {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer expired-token");
    when(authClient.isTokenValid("expired-token"))
        .thenThrow(new CustomUnauthorizedException("Token expired"));

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should not propagate CustomUnauthorizedException up the chain")
  void doFilterInternal_whenCustomUnauthorizedException_shouldNotPropagateException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer bad-token");
    when(authClient.isTokenValid("bad-token"))
        .thenThrow(new CustomUnauthorizedException("Unauthorized"));

    assertThatNoException()
        .isThrownBy(() -> filter.doFilterInternal(request, response, filterChain));
  }

  // ─── CustomBadGatewayException ────────────────────────────────────────────

  @Test
  @DisplayName(
      "Should clear SecurityContext and continue chain when CustomBadGatewayException is thrown")
  void doFilterInternal_whenCustomBadGatewayException_shouldClearContextAndContinueChain()
      throws Exception {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer some-token");
    when(authClient.isTokenValid("some-token"))
        .thenThrow(new CustomBadGatewayException("Auth service unreachable"));

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should not propagate CustomBadGatewayException up the chain")
  void doFilterInternal_whenCustomBadGatewayException_shouldNotPropagateException() {
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer some-token");
    when(authClient.isTokenValid("some-token"))
        .thenThrow(new CustomBadGatewayException("Gateway error"));

    assertThatNoException()
        .isThrownBy(() -> filter.doFilterInternal(request, response, filterChain));
  }

  // ─── SecurityContext isolation ────────────────────────────────────────────

  @Test
  @DisplayName("Should not overwrite existing SecurityContext authentication when header is absent")
  void doFilterInternal_whenNoHeader_shouldNotClearExistingAuthentication() throws Exception {
    UsernamePasswordAuthenticationToken existing =
        new UsernamePasswordAuthenticationToken("existing-user", null, List.of());
    SecurityContextHolder.getContext().setAuthentication(existing);

    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existing);
  }

  @Test
  @DisplayName(
      "Should replace existing SecurityContext authentication when a new valid token is provided")
  void doFilterInternal_whenValidToken_shouldReplaceExistingAuthentication() throws Exception {
    UsernamePasswordAuthenticationToken existing =
        new UsernamePasswordAuthenticationToken("old-user", null, List.of());
    SecurityContextHolder.getContext().setAuthentication(existing);

    UserResponse newUser = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer new-token");
    when(authClient.isTokenValid("new-token")).thenReturn(newUser);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
        .isEqualTo(newUser);
  }
}

package com.sportspulse.teams.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sportspulse.teams.integration.msauth.AuthClient;
import com.sportspulse.teams.integration.msauth.dto.UserResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  void clearSecurityContextAfter() {
    SecurityContextHolder.clearContext();
  }

  // -------------------------------------------------------------------------
  // No Authorization header / malformed header — filter passes through
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() continues chain when Authorization header is absent")
  void doFilterInternal_whenNoAuthorizationHeader_continuesChain() throws Exception {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal() continues chain when Authorization header has no Bearer prefix")
  void doFilterInternal_whenHeaderLacksBearerPrefix_continuesChain() throws Exception {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Basic dXNlcjpwYXNz");

    filter.doFilterInternal(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal() does not call AuthClient when Authorization header is absent")
  void doFilterInternal_whenNoAuthorizationHeader_doesNotCallAuthClient() throws Exception {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    then(authClient).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("doFilterInternal() does not call AuthClient when header lacks Bearer prefix")
  void doFilterInternal_whenHeaderLacksBearerPrefix_doesNotCallAuthClient() throws Exception {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Basic dXNlcjpwYXNz");

    filter.doFilterInternal(request, response, filterChain);

    then(authClient).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // Valid Bearer token — authentication is set
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() sets authentication in SecurityContext for a valid token")
  void doFilterInternal_whenValidBearerToken_setsAuthentication() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer valid-token");
    given(authClient.isTokenValid("valid-token")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth).isNotNull();
    assertThat(auth.getPrincipal()).isEqualTo(user);
  }

  @Test
  @DisplayName("doFilterInternal() continues chain after setting authentication for a valid token")
  void doFilterInternal_whenValidBearerToken_continuesChain() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer valid-token");
    given(authClient.isTokenValid("valid-token")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() strips the 'Bearer ' prefix before calling AuthClient")
  void doFilterInternal_whenValidBearerToken_stripsPrefix() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "ADMIN");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer raw-token-value");
    given(authClient.isTokenValid("raw-token-value")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    then(authClient).should().isTokenValid("raw-token-value");
  }

  @Test
  @DisplayName(
      "doFilterInternal() assigns ROLE_USER and AUTH_JWT authority for a user with role USER")
  void doFilterInternal_whenRoleIsUser_assignsRoleUserAuthority() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer token");
    given(authClient.isTokenValid("token")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_USER", "AUTH_JWT");
  }

  @Test
  @DisplayName(
      "doFilterInternal() assigns ROLE_ADMIN and AUTH_JWT authority for a user with role ADMIN")
  void doFilterInternal_whenRoleIsAdmin_assignsRoleAdminAuthority() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "ADMIN");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer admin-token");
    given(authClient.isTokenValid("admin-token")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_ADMIN", "AUTH_JWT");
  }

  @Test
  @DisplayName("doFilterInternal() sets authentication details from the request")
  void doFilterInternal_whenValidBearerToken_setsAuthenticationDetails() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer token");
    given(authClient.isTokenValid("token")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getDetails()).isNotNull();
  }

  @Test
  @DisplayName("doFilterInternal() wraps UserResponse as the authentication principal")
  void doFilterInternal_whenValidBearerToken_principalIsUserResponse() throws Exception {
    UserResponse user = new UserResponse(true, UUID.randomUUID(), "test-user", "USER");
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer token");
    given(authClient.isTokenValid("token")).willReturn(user);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getPrincipal()).isInstanceOf(UserResponse.class);
  }

  // -------------------------------------------------------------------------
  // AuthClient throws — exception propagates
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() propagates exception thrown by AuthClient")
  void doFilterInternal_whenAuthClientThrows_propagatesException() {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer bad-token");
    given(authClient.isTokenValid("bad-token"))
        .willThrow(new RuntimeException("Token validation failed"));

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Token validation failed");
  }

  @Test
  @DisplayName("doFilterInternal() does not set authentication when AuthClient throws")
  void doFilterInternal_whenAuthClientThrows_doesNotSetAuthentication() {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer bad-token");
    given(authClient.isTokenValid("bad-token"))
        .willThrow(new RuntimeException("Token validation failed"));

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain));

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal() does not continue chain when AuthClient throws")
  void doFilterInternal_whenAuthClientThrows_doesNotContinueChain() {
    given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer bad-token");
    given(authClient.isTokenValid("bad-token"))
        .willThrow(new RuntimeException("Token validation failed"));

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain));

    then(filterChain).shouldHaveNoInteractions();
  }
}

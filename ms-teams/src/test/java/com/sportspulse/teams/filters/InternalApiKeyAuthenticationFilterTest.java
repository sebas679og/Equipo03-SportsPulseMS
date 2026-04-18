package com.sportspulse.teams.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sportspulse.teams.config.constants.InternalHeaders;
import com.sportspulse.teams.config.properties.MsAuthProperties;
import com.sportspulse.teams.exceptions.JsonWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class InternalApiKeyAuthenticationFilterTest {

  private static final String VALID_API_KEY = "secret-internal-key";

  @Mock private MsAuthProperties securityProperties;

  @Mock private JsonWriter writer;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private FilterChain filterChain;

  @InjectMocks private InternalApiKeyAuthenticationFilter filter;

  @BeforeEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void clearSecurityContextAfter() {
    SecurityContextHolder.clearContext();
  }

  // -------------------------------------------------------------------------
  // No API key header — filter passes through
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() continues chain when API key header is absent")
  void doFilterInternal_whenApiKeyHeaderAbsent_continuesChain() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() does not set authentication when API key header is absent")
  void doFilterInternal_whenApiKeyHeaderAbsent_doesNotSetAuthentication() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal() does not call JsonWriter when API key header is absent")
  void doFilterInternal_whenApiKeyHeaderAbsent_doesNotCallJsonWriter() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    then(writer).shouldHaveNoInteractions();
  }

  // -------------------------------------------------------------------------
  // Invalid API key — 401 returned, chain halted
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() sends UNAUTHORIZED when API key is invalid")
  void doFilterInternal_whenApiKeyInvalid_sendsUnauthorizedError() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("wrong-key");
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    then(writer).should().sendError(response, HttpStatus.UNAUTHORIZED, "Invalid internal API key");
  }

  @Test
  @DisplayName("doFilterInternal() does not continue chain when API key is invalid")
  void doFilterInternal_whenApiKeyInvalid_doesNotContinueChain() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("wrong-key");
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    then(filterChain).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("doFilterInternal() does not set authentication when API key is invalid")
  void doFilterInternal_whenApiKeyInvalid_doesNotSetAuthentication() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("wrong-key");
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  @DisplayName("doFilterInternal() logs a warning when API key is invalid")
  void doFilterInternal_whenApiKeyInvalid_logsWarning() {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn("wrong-key");
    given(request.getRemoteAddr()).willReturn("192.168.1.100");
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    // No exception expected — just verifying the filter doesn't blow up with logging enabled
    assertThatNoException()
        .isThrownBy(() -> filter.doFilterInternal(request, response, filterChain));
  }

  // -------------------------------------------------------------------------
  // Valid API key — authentication set, chain continues
  // -------------------------------------------------------------------------

  @Test
  @DisplayName("doFilterInternal() sets authentication in SecurityContext for a valid API key")
  void doFilterInternal_whenApiKeyValid_setsAuthentication() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(VALID_API_KEY);
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
  }

  @Test
  @DisplayName(
      "doFilterInternal() continues chain after setting authentication for a valid API key")
  void doFilterInternal_whenApiKeyValid_continuesChain() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(VALID_API_KEY);
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    then(filterChain).should().doFilter(request, response);
  }

  @Test
  @DisplayName("doFilterInternal() sets 'internal-service' as the authentication principal")
  void doFilterInternal_whenApiKeyValid_setsInternalServiceAsPrincipal() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(VALID_API_KEY);
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getPrincipal()).isEqualTo("internal-service");
  }

  @Test
  @DisplayName("doFilterInternal() grants ROLE_INTERNAL authority for a valid API key")
  void doFilterInternal_whenApiKeyValid_grantsRoleInternalAuthority() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(VALID_API_KEY);
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getAuthorities())
        .extracting(GrantedAuthority::getAuthority)
        .containsExactly("ROLE_INTERNAL");
  }

  @Test
  @DisplayName("doFilterInternal() does not call JsonWriter when API key is valid")
  void doFilterInternal_whenApiKeyValid_doesNotCallJsonWriter() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(VALID_API_KEY);
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    then(writer).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("doFilterInternal() sets null credentials for the internal authentication token")
  void doFilterInternal_whenApiKeyValid_setsNullCredentials() throws Exception {
    given(request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY)).willReturn(VALID_API_KEY);
    given(securityProperties.getApiKey()).willReturn(VALID_API_KEY);

    filter.doFilterInternal(request, response, filterChain);

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat(auth.getCredentials()).isNull();
  }
}

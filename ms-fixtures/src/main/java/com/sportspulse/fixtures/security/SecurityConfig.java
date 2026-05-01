package com.sportspulse.fixtures.security;

import com.sportspulse.fixtures.client.AuthClient;
import com.sportspulse.fixtures.config.properties.AuthProperties;
import com.sportspulse.fixtures.constants.ApiPaths;
import com.sportspulse.fixtures.constants.ErrorConstants;
import com.sportspulse.fixtures.constants.HttpHeaders;
import com.sportspulse.fixtures.security.filter.AuthenticationFilter;
import com.sportspulse.fixtures.security.filter.InternalKeyAuthenticationFilter;
import com.sportspulse.fixtures.security.writer.ErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Central security configuration for the Fixtures microservice.
 *
 * <p>This class establishes a stateless security architecture that supports a dual-authentication
 * strategy: User-based tokens (JWT) and Internal Service keys (S2S). It ensures that all endpoints
 * are protected based on the principle of least privilege.
 *
 * <h3>Filter Chain Orchestration</h3>
 *
 * <ol>
 *   <li><b>Dual Header Guard:</b> Prevents credential ambiguity by rejecting requests containing
 *       both a Bearer token and an Internal API Key.
 *   <li><b>Authentication Filter:</b> Validates JWT tokens against the remote identity provider.
 *   <li><b>Internal Key Filter:</b> Validates static API keys for trusted service-to-service
 *       communication.
 * </ol>
 *
 * <h3>Security Policies</h3>
 *
 * <ul>
 *   <li><b>Statelessness:</b> Disables HTTP sessions to ensure each request is independently
 *       authenticated.
 *   <li><b>Access Obfuscation:</b> Maps Access Denied events to 404 Not Found responses to prevent
 *       resource enumeration by unauthorized actors.
 *   <li><b>Standardized Errors:</b> Utilizes a custom ErrorWriter to maintain JSON consistency
 *       across all security-level failures.
 * </ul>
 *
 * @see AuthenticationFilter
 * @see InternalKeyAuthenticationFilter
 * @see ErrorWriter
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthClient authClient;
  private final AuthProperties authProperties;
  private final ErrorWriter writer;

  /**
   * Configures the security filter chain, defining the protocol for CSRF, session management, and
   * endpoint authorization.
   *
   * <p>This configuration enforces statelessness and maps specific authorities to the fixture
   * endpoints, while allowing public access to documentation and health checks. It also
   * orchestrates the custom filter order.
   *
   * @param http the {@link HttpSecurity} to modify.
   * @return the built {@link SecurityFilterChain}.
   * @throws Exception if an error occurs during configuration.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ApiPaths.Docs.SWAGGER_UI, ApiPaths.Docs.API_DOCS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Fixtures.ACTUATOR_HEALTH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Fixtures.FIXTURES)
                    .hasAnyAuthority(
                        HttpHeaders.Auth.ROLE_AUTH_JWT, HttpHeaders.Auth.ROLE_AUTH_INTERNAL)
                    .requestMatchers(HttpMethod.GET, ApiPaths.Fixtures.FIXTURE_EVENTS)
                    .hasAnyAuthority(
                        HttpHeaders.Auth.ROLE_AUTH_JWT, HttpHeaders.Auth.ROLE_AUTH_INTERNAL)
                    .requestMatchers(HttpMethod.GET, ApiPaths.Fixtures.FIXTURE_LIVE)
                    .hasAnyAuthority(
                        HttpHeaders.Auth.ROLE_AUTH_JWT, HttpHeaders.Auth.ROLE_AUTH_INTERNAL)
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(dualHeaderGuardFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(authenticationFilter(), dualHeaderGuardFilter().getClass())
        .addFilterAfter(internalKeyAuthenticationFilter(), AuthenticationFilter.class)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(unauthorizedEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()))
        .build();
  }

  /**
   * Creates a guard filter that prevents security context collisions.
   *
   * <p>This filter checks for the presence of multiple authentication headers. If both a Bearer
   * token and an Internal API Key are provided, it rejects the request to avoid credential
   * ambiguity and potential privilege escalation.
   *
   * @return a {@link OncePerRequestFilter} that validates header exclusivity.
   */
  @Bean
  public OncePerRequestFilter dualHeaderGuardFilter() {
    return new OncePerRequestFilter() {

      @Override
      protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain)
          throws ServletException, IOException {

        boolean hasBearer =
            request.getHeader(org.springframework.http.HttpHeaders.AUTHORIZATION) != null;
        boolean hasApiKey = request.getHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY) != null;

        if (hasBearer && hasApiKey) {
          writer.sendError(
              response,
              HttpStatus.UNAUTHORIZED,
              ErrorConstants.Message.AUTHENTICATION_NOT_OBTAINED);
          return;
        }

        filterChain.doFilter(request, response);
      }
    };
  }

  /**
   * Bean for the JWT-based authentication filter.
   *
   * @return an instance of {@link AuthenticationFilter} configured with the auth client.
   */
  @Bean
  public AuthenticationFilter authenticationFilter() {
    return new AuthenticationFilter(authClient);
  }

  /**
   * Bean for the internal service key authentication filter.
   *
   * @return an instance of {@link InternalKeyAuthenticationFilter} for S2S security.
   */
  @Bean
  public InternalKeyAuthenticationFilter internalKeyAuthenticationFilter() {
    return new InternalKeyAuthenticationFilter(authProperties, writer);
  }

  /**
   * Defines the entry point for unauthenticated requests.
   *
   * <p>When a client attempts to access a protected resource without credentials, this handler
   * returns a standardized 401 Unauthorized response via the {@link ErrorWriter}.
   *
   * @return a custom {@link AuthenticationEntryPoint}.
   */
  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, ex) ->
        writer.sendError(
            response, HttpStatus.UNAUTHORIZED, ErrorConstants.Message.AUTHENTICATION_REQUIRED);
  }

  /**
   * Defines the handler for authenticated users who lack the required authorities.
   *
   * <p>This implementation uses security by obfuscation, returning a 404 Not Found instead of a 403
   * Forbidden to prevent unauthorized users from confirming the existence of restricted endpoints.
   *
   * @return a custom {@link AccessDeniedHandler}.
   */
  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, ex) ->
        writer.sendError(response, HttpStatus.NOT_FOUND, ErrorConstants.Message.RESOURCE_NOT_FOUND);
  }
}

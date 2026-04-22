package com.sportspulse.teams.config;

import com.sportspulse.teams.config.constants.ApiPaths;
import com.sportspulse.teams.config.constants.InternalHeaders;
import com.sportspulse.teams.config.properties.MsAuthProperties;
import com.sportspulse.teams.exceptions.JsonWriter;
import com.sportspulse.teams.filters.BearerAuthenticationFilter;
import com.sportspulse.teams.filters.InternalApiKeyAuthenticationFilter;
import com.sportspulse.teams.integration.msauth.AuthClient;
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
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * SecurityConfig Configures application-level security settings. Enables method-level security with
 * support for {@code @Secured} annotations to protect service methods and enforce authorization
 * rules.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final JsonWriter writer;
  private final AuthClient authClient;
  private final MsAuthProperties securityProperties;

  /**
   * Configures the {@link SecurityFilterChain} for HTTP security.
   *
   * <p>- Disables CSRF protection for stateless APIs. <br>
   * - Configures session management to be stateless. <br>
   * - Permits health check requests while requiring authentication for all other endpoints. <br>
   * - Adds custom filters in the following order:
   *
   * <ul>
   *   <li>{@code dualHeaderGuardFilter} to reject requests containing both Bearer and API Key
   *       headers.
   *   <li>{@code bearerAuthenticationFilter} to handle Bearer token authentication.
   *   <li>{@code internalApiKeyAuthenticationFilter} to handle internal API key authentication.
   * </ul>
   *
   * <p>- Configures exception handling with a custom unauthorized entry point.
   *
   * @param http the {@link HttpSecurity} to configure
   * @return the configured {@link SecurityFilterChain}
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ApiPaths.Docs.SWAGGER_UI, ApiPaths.Docs.API_DOCS)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Teams.ACTUATOR_HEALTH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Teams.TEAM_BY_ID)
                    .hasAnyAuthority("AUTH_JWT", "AUTH_INTERNAL")
                    .requestMatchers(HttpMethod.GET, ApiPaths.Teams.TEAMS)
                    .hasAuthority("AUTH_JWT")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(dualHeaderGuardFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(bearerAuthenticationFilter(), dualHeaderGuardFilter().getClass())
        .addFilterAfter(internalApiKeyAuthenticationFilter(), BearerAuthenticationFilter.class)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(unauthorizedEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()));

    return http.build();
  }

  /**
   * Creates a filter that prevents requests from including both Bearer and API Key headers
   * simultaneously.
   *
   * <p>If both headers are present, the request is rejected with an {@code UNAUTHORIZED} response.
   * Otherwise, the request continues through the filter chain.
   *
   * @return a {@link OncePerRequestFilter} that enforces mutual exclusivity of authentication
   *     headers
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

        boolean hasBearer = request.getHeader(InternalHeaders.MsAuth.BEARER_HEADER) != null;
        boolean hasApiKey = request.getHeader(InternalHeaders.MsAuth.MS_AUTH_KEY) != null;

        if (hasBearer && hasApiKey) {
          writer.sendError(
              response, HttpStatus.UNAUTHORIZED, "Authentication could not be obtained");
          return;
        }

        filterChain.doFilter(request, response);
      }
    };
  }

  @Bean
  public BearerAuthenticationFilter bearerAuthenticationFilter() {
    return new BearerAuthenticationFilter(authClient);
  }

  @Bean
  public InternalApiKeyAuthenticationFilter internalApiKeyAuthenticationFilter() {
    return new InternalApiKeyAuthenticationFilter(securityProperties, writer);
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) ->
        writer.sendError(response, HttpStatus.UNAUTHORIZED, "Authentication required");
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, ex) ->
        writer.sendError(response, HttpStatus.NOT_FOUND, "The requested resource does not exist");
  }
}

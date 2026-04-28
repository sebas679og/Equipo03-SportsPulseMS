package com.sportspulse.standings.config;

import com.sportspulse.standings.config.constants.ApiPaths;
import com.sportspulse.standings.config.constants.InternalHeaders;
import com.sportspulse.standings.exceptions.JsonWriter;
import com.sportspulse.standings.filters.BearerAuthenticationFilter;
import com.sportspulse.standings.integrations.msauth.AuthClient;
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

  /**
   * Configures the application's security filter chain.
   *
   * <p>Defines security policies including CSRF disabling, stateless session management, request
   * authorization rules, and custom authentication filters.
   *
   * <p>Permits access to Swagger UI and API documentation, as well as health checks. Restricts
   * access to league classification and team position endpoints based on required authorities. Adds
   * custom filters for dual-header guard and bearer authentication, and configures exception
   * handling for unauthorized and access-denied scenarios.
   *
   * @param http the {@link HttpSecurity} to configure
   * @return the built {@link SecurityFilterChain} enforcing application security rules
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
                    .requestMatchers(HttpMethod.GET, ApiPaths.Standings.ACTUATOR_HEALTH)
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET, ApiPaths.Standings.CLASSIFICATION_LEAGUE_IN_A_SEASON)
                    .hasAnyAuthority("AUTH_JWT", "AUTH_INTERNAL")
                    .requestMatchers(
                        HttpMethod.GET, ApiPaths.Standings.POSITION_TEAM_IN_THE_STANDINGS)
                    .hasAuthority("AUTH_JWT")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(dualHeaderGuardFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(bearerAuthenticationFilter(), dualHeaderGuardFilter().getClass())
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(unauthorizedEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()));

    return http.build();
  }

  /**
   * Creates a filter that guards against requests containing both internal and bearer
   * authentication headers.
   *
   * <p>This filter checks for the presence of the internal bearer header defined in {@link
   * InternalHeaders.MsAuth#BEARER_HEADER}. If found, the request is rejected with an {@link
   * HttpStatus#UNAUTHORIZED} response. Otherwise, the request continues through the filter chain.
   *
   * @return a {@link OncePerRequestFilter} enforcing dual-header guard logic
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

        if (hasBearer) {
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

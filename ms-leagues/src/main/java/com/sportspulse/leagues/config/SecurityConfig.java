package com.sportspulse.leagues.config;

import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.exceptions.JsonWriter;
import com.sportspulse.leagues.filters.BearerAuthenticationFilter;
import com.sportspulse.leagues.integrations.msauth.AuthClient;
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

/** Security configuration for the application. */
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
                    .requestMatchers(HttpMethod.GET, ApiPaths.State.HEALTH)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Leagues.LEAGUES_BY_FILTER)
                    .hasAuthority("AUTH_JWT")
                    .requestMatchers(HttpMethod.GET, ApiPaths.Leagues.LEAGUE_BY_ID)
                    .hasAuthority("AUTH_JWT")
                    .anyRequest()
                    .authenticated())
        .addFilterAfter(bearerAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            ex ->
                ex.authenticationEntryPoint(unauthorizedEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler()));

    return http.build();
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

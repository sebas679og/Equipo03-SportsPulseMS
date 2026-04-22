package com.sportspulse.teams.config;

import com.sportspulse.teams.constants.ApiPaths;
import com.sportspulse.teams.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the Sports Pulse Teams microservice.
 *
 * <p>Defines the HTTP security filter chain, disables CSRF for stateless REST APIs, enforces JWT
 * authentication on protected endpoints and registers the {@link JwtAuthenticationFilter} before
 * Spring's default authentication filter.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtFilter;

  /**
   * Configures the {@link SecurityFilterChain} for HTTP request authorization.
   *
   * <p>Public endpoints: Swagger UI and API docs.
   *
   * <p>Protected endpoints: all {@code /api/teams/**} routes require a valid JWT.
   *
   * @param http the {@link HttpSecurity} to configure.
   * @return the configured {@link SecurityFilterChain}.
   * @throws Exception if an error occurs during configuration.
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http.sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        ApiPaths.Docs.SWAGGER_UI,
                        ApiPaths.Docs.API_DOCS,
                        ApiPaths.Docs.SWAGGER_UI_HTML)
                    .permitAll()
                    .requestMatchers(ApiPaths.Team.BASE)
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}

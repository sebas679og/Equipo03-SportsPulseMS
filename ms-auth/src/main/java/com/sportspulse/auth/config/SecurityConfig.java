package com.sportspulse.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 *
 * <p>Defines HTTP security rules, disables stateful features, and configures endpoint access
 * policies. Only specific public endpoints are allowed, while all other requests are denied by
 * default.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  /**
   * Configures the security filter chain.
   *
   * <p>- Disables session creation (stateless API) - Disables CSRF protection (suitable for REST
   * APIs) - Adds basic security headers - Defines authorization rules for endpoints
   *
   * @param http the {@link HttpSecurity} to configure
   * @return the configured {@link SecurityFilterChain}
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(
            headers ->
                headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .cacheControl(Customizer.withDefaults()))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ApiPaths.Docs.SWAGGER_UI, ApiPaths.Docs.API_DOCS)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.REGISTER)
                    .permitAll()
                    .anyRequest()
                    .denyAll());

    return http.build();
  }

  /**
   * Provides a password encoder bean.
   *
   * <p>Uses BCrypt hashing algorithm to securely store user passwords.
   *
   * @return the {@link PasswordEncoder} implementation
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}

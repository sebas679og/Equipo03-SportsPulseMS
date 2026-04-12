package com.sportspulse.auth.config;

import com.sportspulse.auth.config.constants.ApiPaths;
import com.sportspulse.auth.config.properties.JwtProperties;
import com.sportspulse.auth.utils.security.filter.InternalApiKeyFilter;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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

  private final JwtProperties jwtProperties;
  private final InternalApiKeyFilter internalApiKeyFilter;

  /**
   * Configures the security filter chain.
   *
   * <p>- Disables session creation (stateless API) - Disables CSRF protection (suitable for REST
   * APIs) - Adds basic security headers - Defines authorization rules for endpoints
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
                    .requestMatchers(HttpMethod.GET, ApiPaths.State.HEALTH)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.REGISTER)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.LOGIN)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Validate.TOKEN)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /** Provides a password encoder bean. */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}

package com.sportspulse.leagues.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportspulse.leagues.config.constants.ApiPaths;
import com.sportspulse.leagues.dto.responses.ErrorResponse;
import com.sportspulse.leagues.utils.security.filter.AuthValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Security configuration for the application. */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthValidationFilter authValidationFilter;
  private final ObjectMapper objectMapper;

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
                    .requestMatchers(ApiPaths.State.HEALTH)
                    .permitAll()
                    .requestMatchers(ApiPaths.Leagues.BASE + "/**")
                    .hasAnyRole("USER", "ADMIN")
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    (request, response, ex) -> {
                      response.setStatus(HttpStatus.UNAUTHORIZED.value());
                      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                      ErrorResponse body =
                          ErrorResponse.builder()
                              .code(HttpStatus.UNAUTHORIZED.value())
                              .name(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                              .description("Authentication required")
                              .build();
                      response.getWriter().write(objectMapper.writeValueAsString(body));
                    }))
        .addFilterBefore(authValidationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}

package com.sportspulse.gateway.config;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/** SecurityConfig Configures WebFlux security settings for the gateway. */
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  /**
   * Defines the security filter chain for handling authentication and authorization. Disables CSRF,
   * HTTP Basic, and form login, while configuring access rules and custom error handling for
   * unauthorized and forbidden requests.
   *
   * @param http the ServerHttpSecurity configuration
   * @param responseWriter the JsonResponseWriter used to write error responses
   * @return the configured SecurityWebFilterChain
   */
  @Bean
  public SecurityWebFilterChain securityFilterChain(
      ServerHttpSecurity http, JsonResponseWriter responseWriter) {
    return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
        .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
        .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
        .authorizeExchange(
            exchanges ->
                exchanges
                    .pathMatchers(ApiPathsServices.Auth.VALIDATE_TOKEN)
                    .denyAll()
                    .anyExchange()
                    .permitAll())
        .exceptionHandling(
            ex ->
                ex.accessDeniedHandler(
                        (exchange, denied) -> {
                          if (log.isWarnEnabled()) {
                            log.warn(
                                "[SecurityWebFilterChain] Access denied — path='{}'",
                                exchange.getRequest().getPath().value());
                          }
                          ;
                          return responseWriter.write(
                              exchange, HttpStatus.FORBIDDEN, "Access denied");
                        })
                    .authenticationEntryPoint(
                        (exchange, authEx) -> {
                          if (log.isWarnEnabled()) {
                            log.warn(
                                "[SecurityWebFilterChain] Unauthorized access — path='{}'",
                                exchange.getRequest().getPath().value());
                          }
                          return responseWriter.write(
                              exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
                        }))
        .build();
  }
}

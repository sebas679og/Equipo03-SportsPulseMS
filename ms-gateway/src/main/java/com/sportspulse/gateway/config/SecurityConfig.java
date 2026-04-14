package com.sportspulse.gateway.config;

import com.sportspulse.gateway.config.constants.ApiPathsServices;
import com.sportspulse.gateway.config.constants.InternalHeaders;
import com.sportspulse.gateway.config.properties.CorsConfigurationProperties;
import com.sportspulse.gateway.exceptions.JsonResponseWriter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
      ServerHttpSecurity http,
      JsonResponseWriter responseWriter,
      CorsConfigurationSource corsConfigurationSource) {
    return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
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
                                "Access denied — path='{}'",
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
                                "Unauthorized access — path='{}'",
                                exchange.getRequest().getPath().value());
                          }
                          return responseWriter.write(exchange, HttpStatus.NOT_FOUND, "Not found");
                        }))
        .build();
  }

  /**
   * Configures the Cross-Origin Resource Sharing (CORS) settings for the application.
   *
   * <p>This bean defines a global CORS policy that allows controlled access from external origins,
   * configuring allowed methods, headers, and the preflight cache duration.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      CorsConfigurationProperties corsProperties) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(corsProperties.getAllowedOrigins());
    config.setAllowedMethods(corsProperties.getAllowedMethods());
    config.setAllowedHeaders(List.of(InternalHeaders.AUTHORIZATION, InternalHeaders.CONTENT_TYPE));
    config.setMaxAge(corsProperties.getMaxAge());
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}

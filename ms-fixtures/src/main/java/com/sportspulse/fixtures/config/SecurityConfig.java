package com.sportspulse.fixtures.config;

import com.sportspulse.fixtures.config.constants.ApiPaths;
import com.sportspulse.fixtures.exceptions.JsonWriter;
import com.sportspulse.fixtures.filters.BearerAuthenticationFilter;
import com.sportspulse.fixtures.integration.msauth.AuthClient;
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

@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JsonWriter writer;
    private final AuthClient authClient;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(ApiPaths.Docs.SWAGGER_UI, ApiPaths.Docs.API_DOCS)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, ApiPaths.Fixtures.ACTUATOR_HEALTH)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, ApiPaths.Fixtures.FIXTURES)
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

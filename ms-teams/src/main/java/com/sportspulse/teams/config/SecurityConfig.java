package com.sportspulse.teams.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * SecurityConfig Configures application-level security settings. Enables method-level security with
 * support for {@code @Secured} annotations to protect service methods and enforce authorization
 * rules.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {}

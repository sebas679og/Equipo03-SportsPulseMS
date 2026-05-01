package com.sportspulse.fixtures.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AuthProperties Configuration properties for accessing the internal authentication service.
 * Provides the base URL and API key, loaded from application configuration using the prefix {@code
 * sportspulse.teams.services.auth}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sportspulse.fixtures.services.auth")
public class AuthProperties {
  private String baseUrl;
  private String apiKey;
}

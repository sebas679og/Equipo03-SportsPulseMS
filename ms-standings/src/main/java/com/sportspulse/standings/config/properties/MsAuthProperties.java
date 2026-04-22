package com.sportspulse.standings.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MsAuthProperties Configuration properties for accessing the internal authentication service.
 * Provides the base URL and API key, loaded from application configuration using the prefix {@code
 * sportspulse.standings.services.auth}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sportspulse.standings.services.auth")
public class MsAuthProperties {
  private String baseUrl;
  private String apiKey;
}

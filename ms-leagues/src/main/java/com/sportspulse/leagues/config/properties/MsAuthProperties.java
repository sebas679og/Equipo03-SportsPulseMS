package com.sportspulse.leagues.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MsAuthProperties Configuration properties for accessing the internal authentication service.
 * Provides the base URL and API key, loaded from application configuration using the prefix {@code
 * sportspulse.standings.services.auth}.
 */
@Data
@Component
@ConfigurationProperties(prefix = "sportspulse.leagues.services.auth")
public class MsAuthProperties {
  private String baseUrl;
  private String apiKey;
}

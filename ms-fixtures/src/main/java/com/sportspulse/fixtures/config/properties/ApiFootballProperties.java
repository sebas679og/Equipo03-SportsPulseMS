package com.sportspulse.fixtures.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ApiFootballProperties Configuration properties for accessing the Football API. Provides the API
 * key and base URL, loaded from application configuration using the prefix {@code
 * sportspulse.app.api.football}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sportspulse.fixtures.api.football")
public class ApiFootballProperties {
  private String baseUrl;
  private String apiKey;
}

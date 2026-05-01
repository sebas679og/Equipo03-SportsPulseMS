package com.sportspulse.standings.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ApiFootballProperties Configuration properties for accessing the Football API. Provides the API
 * key and base URL, loaded from application configuration using the prefix {@code
 * sportspulse.standings.api.football}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sportspulse.standings.api.football")
public class ApiFootballProperties {
  private String apiKey;
  private String baseUrl;
}

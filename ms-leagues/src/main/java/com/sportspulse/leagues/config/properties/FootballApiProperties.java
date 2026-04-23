package com.sportspulse.leagues.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** External API-Football configuration. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sports-pulse.api")
public class FootballApiProperties {
  private String baseUrl;
  private String key;
}

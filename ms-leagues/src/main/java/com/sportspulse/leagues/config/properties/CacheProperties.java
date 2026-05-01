package com.sportspulse.leagues.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LeaguesCacheProperties Configuration properties for the league caching mechanism. Defines whether
 * caching is enabled, the time-to-live (TTL) in minutes, and the maximum cache size. Values are
 * loaded from application configuration using the prefix {@code sportspulse.standings.cache}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sportspulse.leagues.cache")
public class CacheProperties {
  private boolean enabled;
  private long maxSize;
  private Long ttlMinutes;
}

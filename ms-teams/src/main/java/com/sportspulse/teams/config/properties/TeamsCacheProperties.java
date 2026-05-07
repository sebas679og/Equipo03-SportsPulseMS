package com.sportspulse.teams.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TeamsCacheProperties Configuration properties for the teams caching mechanism. Defines whether
 * caching is enabled, the time-to-live (TTL) in minutes, and the maximum cache size. Values are
 * loaded from application configuration using the prefix {@code sportspulse.teams.cache}.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sportspulse.teams.cache")
public class TeamsCacheProperties {
  private boolean enabled;
  private long ttlMinutes;
  private long maxSize;
}

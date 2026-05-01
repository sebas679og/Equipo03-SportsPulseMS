package com.sportspulse.fixtures.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * CacheProperties Configuration properties for the teams caching mechanism. Defines whether caching
 * is enabled, the time-to-live (TTL) in minutes, and the maximum cache size. Values are loaded from
 * application configuration using the prefix {@code sportspulse.teams.cache}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sportspulse.fixtures.cache")
public class CacheProperties {
  private long maxSize;
  private long ttlMinutes;
  private boolean enabled;
}

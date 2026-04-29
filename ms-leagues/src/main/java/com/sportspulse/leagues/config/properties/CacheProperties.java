package com.sportspulse.leagues.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sportspulse.leagues.cache")
public class CacheProperties {
  private boolean enabled;
  private int maxSize;
  private int ttlMinutes;
}

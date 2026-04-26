package com.sportspulse.leagues.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sportspulse.leagues.services.auth")
public class MsAuthProperties {
  private String baseUrl;
  private String apiKey;
}
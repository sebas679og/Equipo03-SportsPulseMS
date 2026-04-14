package com.sportspulse.gateway.config.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties class for Cross-Origin Resource Sharing (CORS) settings of the
 * application.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sportspulse.app.cors")
public class CorsConfigurationProperties {
  List<String> allowedOrigins;
  List<String> allowedMethods;
  long maxAge;
}

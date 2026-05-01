package com.sportspulse.auth.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** JwtProperties Holds configuration properties for JWT authentication. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

  private String secret;
  private long expiration;
  private String tokenType;
}

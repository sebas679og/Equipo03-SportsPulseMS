package com.sportspulse.gateway.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** GatewayServicesProperties Holds configuration properties for gateway service endpoints. */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sportspulse.gateway.services")
public class GatewayServicesProperties {

  private String auth;
  private String leagues;
  private String fixtures;
  private String teams;
  private String standings;
  private String notifications;
  private String dashboard;
}

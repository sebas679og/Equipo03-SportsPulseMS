package com.sportspulse.leagues.config;

import com.sportspulse.leagues.config.constants.InternalHeaders;
import com.sportspulse.leagues.config.properties.FootballApiProperties;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;

/** Rest client configuration for outbound HTTP calls. */
@Configuration
public class RestClientConfig {

  /** Builds a RestTemplate with sensible timeouts for external API calls. */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(10))
        .build();
  }

  /** Builds a WebClient preconfigured for API-Football calls. */
  @Bean("apiFootballWebClient")
  public WebClient apiFootballWebClient(FootballApiProperties footballApiProperties) {
    return WebClient.builder()
        .baseUrl(footballApiProperties.getBaseUrl())
        .defaultHeader(
            InternalHeaders.ApiFootball.API_FOOTBALL_KEY, footballApiProperties.getApiKey())
        .build();
  }
}

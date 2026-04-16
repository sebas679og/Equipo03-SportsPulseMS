package com.sportspulse.teams.config;

import com.sportspulse.teams.config.constants.InternalHeaders;
import com.sportspulse.teams.config.properties.ApiFootballProperties;
import com.sportspulse.teams.config.properties.MsAuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClientConfig Provides configuration for {@link WebClient} instances used to interact with
 * external services such as the Football API and the internal authentication service. Each client
 * is preconfigured with its base URL and required authentication headers.
 */
@Configuration
public class WebClientConfig {

  /**
   * Creates a {@link WebClient} instance for the Football API. Configures the client with the base
   * URL and attaches the API key as a request header for authentication.
   *
   * @param apiFootballProperties the configuration properties containing the Football API base URL
   *     and API key
   * @return a configured {@link WebClient} for the Football API
   */
  @Bean
  public WebClient apiFootballWebClient(ApiFootballProperties apiFootballProperties) {
    return WebClient.builder()
        .baseUrl(apiFootballProperties.getBaseUrl())
        .filter(
            (request, next) -> {
              ClientRequest newRequest =
                  ClientRequest.from(request)
                      .header(
                          InternalHeaders.ApiFootball.API_FOOTBALL_KEY,
                          apiFootballProperties.getApiKey())
                      .build();

              return next.exchange(newRequest);
            })
        .build();
  }

  /**
   * Creates a {@link WebClient} instance for the internal authentication service. Configures the
   * client with the base URL and attaches the internal API key as a request header for secure
   * communication between microservices.
   *
   * @param msAuthProperties the configuration properties containing the authentication service base
   *     URL and API key
   * @return a configured {@link WebClient} for the authentication service
   */
  @Bean
  public WebClient msAuthWebClient(MsAuthProperties msAuthProperties) {
    return WebClient.builder()
        .baseUrl(msAuthProperties.getBaseUrl())
        .filter(
            (request, next) -> {
              ClientRequest newRequest =
                  ClientRequest.from(request)
                      .header(InternalHeaders.MsAuth.MS_AUTH_KEY, msAuthProperties.getApiKey())
                      .build();

              return next.exchange(newRequest);
            })
        .build();
  }
}

package com.sportspulse.fixtures.config;

import com.sportspulse.fixtures.config.properties.ApiFootballProperties;
import com.sportspulse.fixtures.config.properties.AuthProperties;
import com.sportspulse.fixtures.constants.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient beans used across the application.
 *
 * <p>This class defines and configures specific {@link WebClient} instances for communicating with
 * external APIs and internal microservices. Each bean is qualified to ensure the correct
 * configuration is injected into the respective clients.
 *
 * @author Sportspulse Team
 * @version 1.0
 */
@Slf4j
@Configuration
public class ClientConfig {

  /**
   * Configures a WebClient bean for the external Football API.
   *
   * <p>Key configurations include:
   *
   * <ul>
   *   <li><b>Base URL:</b> Derived from {@link ApiFootballProperties}.
   *   <li><b>Max In-Memory Size:</b> Increased to 10MB to handle large JSON payloads common in
   *       sports data fixtures.
   *   <li><b>Auth Headers:</b> Automatically includes the API Key required by the provider.
   * </ul>
   *
   * @param props The properties containing the API base URL and secret key.
   * @return A configured {@link WebClient} specifically for Football data.
   */
  @Bean("footballWebClient")
  public WebClient apiFootballWebClient(ApiFootballProperties props) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .codecs(
            clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
        .defaultHeader(HttpHeaders.ApiFootball.Headers.KEY, props.getApiKey())
        .build();
  }

  /**
   * Configures a WebClient bean for the internal Authentication Microservice.
   *
   * <p>This client is used for inter-service communication and includes the internal security
   * header required for service-to-service authentication.
   *
   * @param props The properties containing the Auth service base URL and internal API key.
   * @return A configured {@link WebClient} for Auth service requests.
   */
  @Bean("authWebClient")
  public WebClient msAuthWebClient(AuthProperties props) {
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .defaultHeader(HttpHeaders.Auth.AUTH_INTERNAL_KEY, props.getApiKey())
        .build();
  }
}

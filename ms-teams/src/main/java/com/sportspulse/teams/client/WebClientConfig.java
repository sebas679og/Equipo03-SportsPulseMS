package com.sportspulse.teams.client;

import com.sportspulse.teams.constants.HttpHeaders;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * WebClient configuration for the API-Football external service.
 *
 * <p>Provides a pre-configured {@link WebClient} bean with base URL,
 * authentication header and connection timeouts.
 */
@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_SECONDS = 5;
    private static final int RESPONSE_TIMEOUT_SECONDS = 10;

    @Value("${sports-pulse.api.base-url}")
    private String baseUrl;

    @Value("${sports-pulse.api.key}")
    private String apiKey;

    /**
     * Creates a {@link WebClient} configured for API-Football requests.
     *
     * @return a WebClient instance with base URL, API key header and timeouts.
     */
    @Bean
    public WebClient footballWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SECONDS))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS).toMillis());

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.X_API_SPORTS_KEY, apiKey)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
